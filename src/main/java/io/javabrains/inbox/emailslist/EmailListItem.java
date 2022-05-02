package io.javabrains.inbox.emailslist;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table(value="emails_by_user_and_folder")
public class EmailListItem {

    @PrimaryKey
    private EmailListItemKey key;

    @CassandraType(type = CassandraType.Name.LIST, typeArguments = CassandraType.Name.TEXT)
    private List<String> to;

    @CassandraType(type = CassandraType.Name.TEXT)
    private String subject;

    @CassandraType(type = CassandraType.Name.BOOLEAN)
    private boolean isUnread;

    @Transient
    private String agoTimeString;

    public EmailListItemKey getKey() {
        return key;
    }

    public void setKey(EmailListItemKey key) {
        this.key = key;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isUnread() {
        return isUnread;
    }

    public void setUnread(boolean unread) {
        isUnread = unread;
    }

    public String getAgoTimeString() {
        return agoTimeString;
    }

    public void setAgoTimeString(String agoTimeString) {
        this.agoTimeString = agoTimeString;
    }
}
