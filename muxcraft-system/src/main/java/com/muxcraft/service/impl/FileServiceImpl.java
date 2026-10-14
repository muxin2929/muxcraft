package com.muxcraft.service.impl;

import com.aliyun.oss.OSS;
import com.muxcraft.common.BizException;
import com.muxcraft.common.ResultCode;
import com.muxcraft.config.FileProperties;
import com.muxcraft.config.OssClientFactory;
import com.muxcraft.service.FileService;
import com.muxcraft.vo.UploadVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传服务实现
 *
 * 上传策略（file.upload-type）：
 *   - oss：优先上传阿里云 OSS，失败或未配置时自动降级到本地存储
 *   - local：直接使用本地存储
 *
 * 存储路径分类（两种方案统一）：
 *   {category}/yyyy/MM/{uuid}.{ext}
 *   - category：功能大类（avatar / cover / icon / common 等）
 *   - yyyy/MM：年月小类
 */
@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> CATEGORY_PREFIXES = Set.of("avatars/", "covers/", "icons/", "common/");
    private static final long AVATAR_MAX_SIZE = 5 * 1024 * 1024;
    private static final long UPLOAD_MAX_SIZE = 2 * 1024 * 1024;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    private final FileProperties fileProperties;
    private final OssClientFactory ossClientFactory;

    public FileServiceImpl(FileProperties fileProperties, OssClientFactory ossClientFactory) {
        this.fileProperties = fileProperties;
        this.ossClientFactory = ossClientFactory;
    }

    @Override
    public UploadVO uploadAvatar(Long userId, MultipartFile file) {
        validateImage(file, AVATAR_MAX_SIZE);
        String ext = getExtension(file.getOriginalFilename());
        String category = "avatars";
        String monthPath = LocalDate.now().format(MONTH_FORMATTER);
        String objectKey = category + "/" + monthPath + "/user-" + userId + "-" + UUID.randomUUID() + "." + ext;
        return saveFile(file, objectKey);
    }

    @Override
    public UploadVO uploadAdmin(MultipartFile file, String type) {
        validateImage(file, UPLOAD_MAX_SIZE);
        String ext = getExtension(file.getOriginalFilename());
        String category = (type == null || type.isBlank()) ? "common" : type;
        String monthPath = LocalDate.now().format(MONTH_FORMATTER);
        String objectKey = category + "/" + monthPath + "/" + UUID.randomUUID() + "." + ext;
        return saveFile(file, objectKey);
    }

    @Override
    public UploadVO upload(MultipartFile file, String category) {
        validateImage(file, UPLOAD_MAX_SIZE);
        String ext = getExtension(file.getOriginalFilename());
        String cat = (category == null || category.isBlank()) ? "common" : category;
        String monthPath = LocalDate.now().format(MONTH_FORMATTER);
        String objectKey = cat + "/" + monthPath + "/" + UUID.randomUUID() + "." + ext;
        return saveFile(file, objectKey);
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        String objectKey = extractObjectKey(fileUrl);
        if (objectKey == null) {
            log.warn("无法从 URL 解析 objectKey，跳过删除: {}", fileUrl);
            return;
        }
        deleteFromOss(objectKey);
        deleteFromLocal(objectKey);
    }

    private String extractObjectKey(String fileUrl) {
        for (String prefix : CATEGORY_PREFIXES) {
            int idx = fileUrl.indexOf(prefix);
            if (idx >= 0) {
                return fileUrl.substring(idx);
            }
        }
        return null;
    }

    private void deleteFromOss(String objectKey) {
        OSS client = ossClientFactory.getClient();
        if (client == null) {
            return;
        }
        try {
            client.deleteObject(ossClientFactory.getBucketName(), objectKey);
            log.info("OSS 文件删除成功: {}", objectKey);
        } catch (Exception e) {
            log.warn("OSS 文件删除失败: {}, error={}", objectKey, e.getMessage());
        }
    }

    private void deleteFromLocal(String objectKey) {
        Path target = Paths.get(fileProperties.getLocalPath(), objectKey).toAbsolutePath();
        try {
            if (Files.deleteIfExists(target)) {
                log.info("本地文件删除成功: {}", objectKey);
            }
        } catch (IOException e) {
            log.warn("本地文件删除失败: {}, error={}", objectKey, e.getMessage());
        }
    }

    private UploadVO saveFile(MultipartFile file, String objectKey) {
        String uploadType = fileProperties.getUploadType();
        if ("local".equalsIgnoreCase(uploadType)) {
            return saveToLocal(file, objectKey);
        }
        UploadVO ossResult = saveToOss(file, objectKey);
        if (ossResult != null) {
            return ossResult;
        }
        log.warn("OSS 上传失败或未配置，降级到本地存储, objectKey={}", objectKey);
        return saveToLocal(file, objectKey);
    }

    /**
     * 上传到阿里云 OSS
     * 成功返回 UploadVO，失败返回 null（交由调用方降级到本地）
     */
    private UploadVO saveToOss(MultipartFile file, String objectKey) {
        OSS client = ossClientFactory.getClient();
        if (client == null) {
            log.info("OSS 未配置，跳过 OSS 上传, objectKey={}", objectKey);
            return null;
        }
        try {
            client.putObject(ossClientFactory.getBucketName(), objectKey, file.getInputStream());
            String url = buildOssUrl(objectKey);
            return new UploadVO(url, objectKey);
        } catch (Exception e) {
            log.error("OSS 上传失败，将降级到本地存储, objectKey={}, error={}", objectKey, e.getMessage(), e);
            return null;
        }
    }

    private String buildOssUrl(String objectKey) {
        String domain = ossClientFactory.getOssDomain();
        if (domain != null && !domain.isEmpty()) {
            if (domain.endsWith("/")) {
                domain = domain.substring(0, domain.length() - 1);
            }
            return domain + "/" + objectKey;
        }
        String bucket = ossClientFactory.getBucketName();
        String endpoint = ossClientFactory.getEndpoint();
        return "https://" + bucket + "." + endpoint + "/" + objectKey;
    }

    /**
     * 本地存储（OSS 故障时的降级方案，也可通过 file.upload-type=local 直接使用）
     * 存储路径：{local-path}/{category}/yyyy/MM/{uuid}.{ext}
     * 访问 URL：{local-url-base}/uploads/{category}/yyyy/MM/{uuid}.{ext}
     */
    private UploadVO saveToLocal(MultipartFile file, String objectKey) {
        Path target = Paths.get(fileProperties.getLocalPath(), objectKey).toAbsolutePath();
        try {
            Path uploadPath = target.getParent();
            if (uploadPath != null) {
                Files.createDirectories(uploadPath);
            }
            file.transferTo(target.toFile());
            String base = fileProperties.getLocalUrlBase();
            if (base == null || base.isEmpty()) {
                base = "";
            } else if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            String url = base + "/uploads/" + objectKey;
            return new UploadVO(url, objectKey);
        } catch (java.nio.file.AccessDeniedException | SecurityException e) {
            log.error("本地文件保存失败：目录无写入权限, objectKey={}, target={}", objectKey, target, e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "文件存储目录无写入权限，请联系管理员检查目录权限");
        } catch (IllegalStateException e) {
            log.error("本地文件保存失败：文件状态异常, objectKey={}, target={}", objectKey, target, e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "文件处理异常，请重新上传");
        } catch (IOException e) {
            log.error("本地文件保存失败, objectKey={}, target={}", objectKey, target, e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "文件保存失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("本地文件保存失败：未知异常, objectKey={}, target={}", objectKey, target, e);
            throw new BizException(ResultCode.INTERNAL_ERROR, "文件保存失败，请稍后重试");
        }
    }

    private void validateImage(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件不能为空");
        }
        if (file.getSize() > maxSize) {
            throw new BizException(ResultCode.PAYLOAD_TOO_LARGE, "文件过大");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new BizException(ResultCode.BAD_REQUEST, "仅支持 JPG/PNG/WEBP/GIF 格式");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BizException(ResultCode.BAD_REQUEST, "文件名无效");
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}