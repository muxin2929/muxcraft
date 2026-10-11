package com.muxcraft.controller;

import com.muxcraft.common.JsonUtils;
import com.muxcraft.common.PageResult;
import com.muxcraft.common.Result;
import com.muxcraft.dto.AdminProfileDTO;
import com.muxcraft.dto.ArticleDTO;
import com.muxcraft.dto.AchievementDTO;
import com.muxcraft.dto.ExperienceDTO;
import com.muxcraft.dto.ProjectDTO;
import com.muxcraft.dto.SiteSettingsDTO;
import com.muxcraft.dto.SkillDTO;
import com.muxcraft.dto.UserStatusDTO;
import com.muxcraft.entity.Achievement;
import com.muxcraft.entity.Article;
import com.muxcraft.entity.Experience;
import com.muxcraft.entity.Project;
import com.muxcraft.entity.Skill;
import com.muxcraft.service.AdminService;
import com.muxcraft.service.ArticleService;
import com.muxcraft.service.AchievementService;
import com.muxcraft.service.CommentService;
import com.muxcraft.service.ExperienceService;
import com.muxcraft.service.FileService;
import com.muxcraft.service.GuestbookService;
import com.muxcraft.service.ProjectService;
import com.muxcraft.service.SiteService;
import com.muxcraft.service.SkillService;
import com.muxcraft.service.SuggestionService;
import com.muxcraft.vo.DashboardVO;
import com.muxcraft.vo.ProfileVO;
import com.muxcraft.vo.SiteSettingsVO;
import com.muxcraft.vo.UploadVO;
import com.muxcraft.vo.UserAdminVO;
import com.muxcraft.vo.ArticleVO;
import com.muxcraft.vo.ExperienceVO;
import com.muxcraft.vo.ProjectVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员控制器
 * 接口：/admin/dashboard、/admin/users、/admin/profile、/admin/settings、
 *       /admin/projects、/admin/experiences、/admin/skills、/admin/articles、
 *       /admin/comments、/admin/guestbook、/admin/suggestions、/admin/files
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final SiteService siteService;
    private final ProjectService projectService;
    private final ExperienceService experienceService;
    private final SkillService skillService;
    private final ArticleService articleService;
    private final AchievementService achievementService;
    private final CommentService commentService;
    private final GuestbookService guestbookService;
    private final SuggestionService suggestionService;
    private final FileService fileService;

    public AdminController(AdminService adminService, SiteService siteService, ProjectService projectService,
                           ExperienceService experienceService, SkillService skillService, ArticleService articleService,
                           AchievementService achievementService, CommentService commentService,
                           GuestbookService guestbookService, SuggestionService suggestionService,
                           FileService fileService) {
        this.adminService = adminService;
        this.siteService = siteService;
        this.projectService = projectService;
        this.experienceService = experienceService;
        this.skillService = skillService;
        this.articleService = articleService;
        this.achievementService = achievementService;
        this.commentService = commentService;
        this.guestbookService = guestbookService;
        this.suggestionService = suggestionService;
        this.fileService = fileService;
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(adminService.dashboard());
    }

    @GetMapping("/users")
    public Result<PageResult<UserAdminVO>> users(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        size = Math.min(size, 50);
        LocalDateTime start = parseDateTime(startTime, false);
        LocalDateTime end = parseDateTime(endTime, true);
        return Result.success(adminService.userPage(page, size, keyword, start, end));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody UserStatusDTO dto) {
        adminService.updateUserStatus(id, dto);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/profile")
    public Result<ProfileVO> getProfile() {
        return Result.success(siteService.getProfile());
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ProfileVO> updateProfile(@RequestParam("data") String dataJson,
                                           @RequestPart(value = "photo", required = false) MultipartFile photo) {
        AdminProfileDTO dto = JsonUtils.parse(dataJson, AdminProfileDTO.class);
        return Result.success(siteService.updateAdminProfile(dto, photo));
    }

    @GetMapping("/settings")
    public Result<SiteSettingsVO> getSettings() {
        return Result.success(adminService.getSettings());
    }

    @PutMapping(value = "/settings", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<SiteSettingsVO> updateSettings(@RequestParam("data") String dataJson,
                                                 @RequestPart(value = "photo", required = false) MultipartFile photo) {
        SiteSettingsDTO dto = JsonUtils.parse(dataJson, SiteSettingsDTO.class);
        return Result.success(adminService.updateSettings(dto, photo));
    }

    @GetMapping("/projects")
    public Result<PageResult<Project>> projects(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.success(projectService.adminPage(page, size));
    }

    @GetMapping("/projects/{id}")
    public Result<Project> getProject(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }

    @PostMapping(value = "/projects", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ProjectVO> createProject(@RequestParam("data") String dataJson,
                                           @RequestPart(value = "icon", required = false) MultipartFile icon,
                                           @RequestPart(value = "cover", required = false) MultipartFile cover) {
        ProjectDTO dto = JsonUtils.parse(dataJson, ProjectDTO.class);
        return Result.success(projectService.create(dto, icon, cover));
    }

    @PutMapping(value = "/projects/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ProjectVO> updateProject(@PathVariable Long id,
                                           @RequestParam("data") String dataJson,
                                           @RequestPart(value = "icon", required = false) MultipartFile icon,
                                           @RequestPart(value = "cover", required = false) MultipartFile cover) {
        ProjectDTO dto = JsonUtils.parse(dataJson, ProjectDTO.class);
        return Result.success(projectService.update(id, dto, icon, cover));
    }

    @DeleteMapping("/projects/{id}")
    public Result<Void> deleteProject(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success();
    }

    @GetMapping("/experiences")
    public Result<PageResult<Experience>> experiences(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.success(experienceService.adminPage(page, size));
    }

    @GetMapping("/experiences/{id}")
    public Result<Experience> getExperience(@PathVariable Long id) {
        return Result.success(experienceService.getById(id));
    }

    @PostMapping(value = "/experiences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ExperienceVO> createExperience(@RequestParam("data") String dataJson,
                                                 @RequestPart(value = "cover", required = false) MultipartFile cover) {
        ExperienceDTO dto = JsonUtils.parse(dataJson, ExperienceDTO.class);
        return Result.success(experienceService.create(dto, cover));
    }

    @PutMapping(value = "/experiences/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ExperienceVO> updateExperience(@PathVariable Long id,
                                                 @RequestParam("data") String dataJson,
                                                 @RequestPart(value = "cover", required = false) MultipartFile cover) {
        ExperienceDTO dto = JsonUtils.parse(dataJson, ExperienceDTO.class);
        return Result.success(experienceService.update(id, dto, cover));
    }

    @DeleteMapping("/experiences/{id}")
    public Result<Void> deleteExperience(@PathVariable Long id) {
        experienceService.delete(id);
        return Result.success();
    }

    @GetMapping("/skills")
    public Result<java.util.List<Skill>> skills() {
        return Result.success(skillService.adminList());
    }

    @GetMapping("/skills/{id}")
    public Result<Skill> getSkill(@PathVariable Long id) {
        return Result.success(skillService.getById(id));
    }

    @PostMapping(value = "/skills", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Skill> createSkill(@RequestParam("data") String dataJson,
                                     @RequestPart(value = "icon", required = false) MultipartFile icon) {
        SkillDTO dto = JsonUtils.parse(dataJson, SkillDTO.class);
        return Result.success(skillService.create(dto, icon));
    }

    @PutMapping(value = "/skills/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Skill> updateSkill(@PathVariable Long id,
                                     @RequestParam("data") String dataJson,
                                     @RequestPart(value = "icon", required = false) MultipartFile icon) {
        SkillDTO dto = JsonUtils.parse(dataJson, SkillDTO.class);
        return Result.success(skillService.update(id, dto, icon));
    }

    @DeleteMapping("/skills/{id}")
    public Result<Void> deleteSkill(@PathVariable Long id) {
        skillService.delete(id);
        return Result.success();
    }

    @GetMapping("/articles")
    public Result<PageResult<Article>> articles(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.success(articleService.adminPage(page, size));
    }

    @GetMapping("/articles/{id}")
    public Result<Article> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }

    @PostMapping(value = "/articles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ArticleVO> createArticle(@RequestParam("data") String dataJson,
                                           @RequestPart(value = "cover", required = false) MultipartFile cover) {
        ArticleDTO dto = JsonUtils.parse(dataJson, ArticleDTO.class);
        return Result.success(articleService.create(dto, cover));
    }

    @PutMapping(value = "/articles/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<ArticleVO> updateArticle(@PathVariable Long id,
                                           @RequestParam("data") String dataJson,
                                           @RequestPart(value = "cover", required = false) MultipartFile cover) {
        ArticleDTO dto = JsonUtils.parse(dataJson, ArticleDTO.class);
        return Result.success(articleService.update(id, dto, cover));
    }

    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    @GetMapping("/comments")
    public Result<PageResult<com.muxcraft.vo.CommentAdminVO>> comments(@RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.success(commentService.adminPage(page, size));
    }

    @DeleteMapping("/comments/batch")
    public Result<Void> deleteCommentsBatch(@RequestParam("ids") List<Long> ids) {
        commentService.deleteByIds(ids);
        return Result.success();
    }

    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }

    @GetMapping("/guestbook")
    public Result<PageResult<com.muxcraft.vo.GuestbookAdminVO>> guestbook(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.success(guestbookService.adminPage(page, size));
    }

    @DeleteMapping("/guestbook/batch")
    public Result<Void> deleteGuestbookBatch(@RequestParam("ids") List<Long> ids) {
        guestbookService.deleteByIds(ids);
        return Result.success();
    }

    @DeleteMapping("/guestbook/{id}")
    public Result<Void> deleteGuestbook(@PathVariable Long id) {
        guestbookService.delete(id);
        return Result.success();
    }

    @GetMapping("/suggestions")
    public Result<PageResult<com.muxcraft.vo.SuggestionAdminVO>> suggestions(@RequestParam(defaultValue = "1") int page,
                                                                             @RequestParam(defaultValue = "10") int size) {
        size = Math.min(size, 50);
        return Result.success(suggestionService.adminPage(page, size));
    }

    @DeleteMapping("/suggestions/batch")
    public Result<Void> deleteSuggestionsBatch(@RequestParam("ids") List<Long> ids) {
        suggestionService.deleteByIds(ids);
        return Result.success();
    }

    @DeleteMapping("/suggestions/{id}")
    public Result<Void> deleteSuggestion(@PathVariable Long id) {
        suggestionService.delete(id);
        return Result.success();
    }

    @PostMapping("/files")
    public Result<UploadVO> uploadFile(@RequestParam("file") MultipartFile file,
                                       @RequestParam(defaultValue = "project") String type) {
        return Result.success("upload success", fileService.uploadAdmin(file, type));
    }

    @GetMapping("/achievements")
    public Result<java.util.List<Achievement>> achievements() {
        return Result.success(achievementService.adminList());
    }

    @GetMapping("/achievements/{id}")
    public Result<Achievement> getAchievement(@PathVariable Long id) {
        return Result.success(achievementService.getById(id));
    }

    @PostMapping("/achievements")
    public Result<Achievement> createAchievement(@Valid @RequestBody AchievementDTO dto) {
        return Result.success(achievementService.create(dto));
    }

    @PutMapping("/achievements/{id}")
    public Result<Achievement> updateAchievement(@PathVariable Long id, @Valid @RequestBody AchievementDTO dto) {
        return Result.success(achievementService.update(id, dto));
    }

    @DeleteMapping("/achievements/{id}")
    public Result<Void> deleteAchievement(@PathVariable Long id) {
        achievementService.delete(id);
        return Result.success();
    }

    private LocalDateTime parseDateTime(String value, boolean isEnd) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() == 10) {
            java.time.LocalDate date = java.time.LocalDate.parse(trimmed);
            return isEnd ? date.atTime(23, 59, 59) : date.atStartOfDay();
        }
        return LocalDateTime.parse(trimmed);
    }
}