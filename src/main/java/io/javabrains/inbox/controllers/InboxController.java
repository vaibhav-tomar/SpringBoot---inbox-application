package io.javabrains.inbox.controllers;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.javabrains.inbox.emailslist.EmailListItem;
import io.javabrains.inbox.emailslist.EmailListItemRepository;
import io.javabrains.inbox.folders.*;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class InboxController {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;
    @Autowired private EmailListItemRepository emailListItemRepository;
    @Autowired private UnreadEmailStatsRepository unreadEmailStatsRepository;

    @GetMapping(value = "/")
    public String homePage(@RequestParam(required = false) String folder, @AuthenticationPrincipal OAuth2User principal, Model model) {

        if(principal == null || !StringUtils.hasText(principal.getAttribute("login"))){
            return "index";
        }

        // Fetch folders
        String userId = principal.getAttribute("login");
        List<Folder> userFolders = folderRepository.findAllById(userId);
        model.addAttribute("userFolders", userFolders);
        List<Folder> defaultFolders = folderService.fetchDefaultFolder(userId);
        model.addAttribute("defaultFolders", defaultFolders);


        model.addAttribute("stats", folderService.mapCountToLabels(userId));

        // Fetch emails
        if(!StringUtils.hasText(folder)){
            folder="Inbox";
        }
        List<EmailListItem> emailList = emailListItemRepository.findAllByKey_IdAndKey_Label(userId, folder);
        PrettyTime p = new PrettyTime();
        emailList.stream().forEach(emailItem -> {
            UUID timeUUID = emailItem.getKey().getTimeUUID();
            Date emailDateTime = new Date(Uuids.unixTimestamp(timeUUID));
            emailItem.setAgoTimeString(p.format(emailDateTime));
        });

        model.addAttribute("emailList", emailList);
        model.addAttribute("folderName", folder);


        return "inbox-page";

    }
}
