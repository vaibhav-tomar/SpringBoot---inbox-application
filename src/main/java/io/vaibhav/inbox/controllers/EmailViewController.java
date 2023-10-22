package io.vaibhav.inbox.controllers;

import io.vaibhav.inbox.email.Email;
import io.vaibhav.inbox.email.EmailRepository;
import io.vaibhav.inbox.emailslist.EmailListItem;
import io.vaibhav.inbox.emailslist.EmailListItemKey;
import io.vaibhav.inbox.emailslist.EmailListItemRepository;
import io.vaibhav.inbox.folders.Folder;
import io.vaibhav.inbox.folders.FolderRepository;
import io.vaibhav.inbox.folders.FolderService;
import io.vaibhav.inbox.folders.UnreadEmailStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class EmailViewController {


    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;
    @Autowired
    private EmailRepository emailRepository;
    @Autowired private EmailListItemRepository emailListItemRepository;
    @Autowired
    private UnreadEmailStatsRepository unreadEmailStatsRepository;

    @GetMapping(value = "/emails/{id}")
    public String emailView(@RequestParam String folder, @PathVariable UUID id, @AuthenticationPrincipal OAuth2User principal, Model model) {


        if (principal == null || !StringUtils.hasText(principal.getAttribute("login"))) {
            return "index";
        }

        // Fetch folders
        String userId = principal.getAttribute("login");
        List<Folder> userFolders = folderRepository.findAllById(userId);
        model.addAttribute("userFolders", userFolders);
        List<Folder> defaultFolders = folderService.fetchDefaultFolder(userId);
        model.addAttribute("defaultFolders", defaultFolders);



        Optional<Email> optionalEmail = emailRepository.findById(id);
        if(!optionalEmail.isPresent()){
            return "inbox-page";
        }
        Email email = optionalEmail.get();
        String toIds = String.join(", ", email.getTo());

        // check if user is allowed to view the email
        if(!userId.equals(email.getFrom()) && !email.getTo().contains(userId)){
            return "redirect:/";
        }

        model.addAttribute("email", email );
        model.addAttribute("toIds", toIds );

        EmailListItemKey key  = new EmailListItemKey();
        key.setId(userId);
        key.setLabel(folder);
        key.setTimeUUID(email.getId());

        Optional<EmailListItem> optionalEmailListItem = emailListItemRepository.findById(key);
        if(optionalEmailListItem.isPresent()){
            EmailListItem emailListItem = optionalEmailListItem.get();
            if(emailListItem.isUnread()){
                emailListItem.setUnread(false);
                emailListItemRepository.save(emailListItem);
                unreadEmailStatsRepository.decrementUnreadCount(userId, folder);
            }
        }
        model.addAttribute("stats", folderService.mapCountToLabels(userId));


        return "email-page";

    }

}
