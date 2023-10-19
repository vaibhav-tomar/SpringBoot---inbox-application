package io.vaibhav.inbox.email;

import com.datastax.oss.driver.api.core.uuid.Uuids;
import io.vaibhav.inbox.emailslist.EmailListItem;
import io.vaibhav.inbox.emailslist.EmailListItemKey;
import io.vaibhav.inbox.emailslist.EmailListItemRepository;
import io.vaibhav.inbox.folders.UnreadEmailStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired private EmailRepository emailRepository;
    @Autowired private EmailListItemRepository emailListItemRepository;
    @Autowired private UnreadEmailStatsRepository unreadEmailStatsRepository;


    public void sendEmail(String from, List<String> to, String subject, String body) {
        Email email = new Email();
        email.setTo(to);
        email.setSubject(subject);
        email.setFrom(from);
        email.setBody(body);
        email.setId(Uuids.timeBased());

        emailRepository.save(email);

        to.forEach(toId -> {
            EmailListItem item = createEmailListItem(to, subject, email, toId, "Inbox");
            emailListItemRepository.save(item);
            unreadEmailStatsRepository.incrementUnreadCount(toId, "Inbox");
        });

        EmailListItem sentItemsEntry = createEmailListItem(to, subject, email, from, "Sent Items");
        sentItemsEntry.setUnread(false);
        emailListItemRepository.save(sentItemsEntry);
    }

    private EmailListItem createEmailListItem(List<String> to, String subject, Email email, String itemOwner, String folder) {
        EmailListItemKey key = new EmailListItemKey();
        key.setId(itemOwner);
        key.setLabel(folder);
        key.setTimeUUID(email.getId());
        EmailListItem item = new EmailListItem();
        item.setKey(key);
        item.setTo(to);
        item.setSubject(subject);
        item.setUnread(true);
        return item;

    }
}
