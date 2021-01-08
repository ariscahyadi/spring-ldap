package sg.ncl.ldap;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.List;

public class LdapExample {

    public List<String> getListing() {
        LdapTemplate template = getTemplate();
        List<String> children = template.list("dc=dev,dc=ncl,dc=sg");
        return children;
    }

    public List<String> getUser(String username) {
        LdapTemplate template = getTemplate();
        List<String> user = template.search(
                "dc=dev,dc=ncl,dc=sg",
                "uid=" + username,
                (AttributesMapper<String>) attrs ->
                        (String) attrs.get("cn").get());
        return user;
    }

    public void deleteUser(String username) {
        LdapTemplate template = getTemplate();
        template.unbind("uid=" + username
                                + ",ou=users,dc=dev,dc=ncl,dc=sg");
        System.out.println("User " + username + " is deleted");
    }

    public void createUser(String username, String password) {
        LdapTemplate template = getTemplate();
        Name dn = LdapNameBuilder.newInstance
                ("uid=" + username + ",ou=users,dc=dev,dc=ncl,dc=sg").build();
        DirContextAdapter context = new DirContextAdapter(dn);

        context.setAttributeValues(
                "objectclass",
                new String[]
                        { "top",
                                "inetOrgPerson",
                                "posixAccount",
                                "shadowAccount" });
        context.setAttributeValue("cn", username);
        context.setAttributeValue("sn", username);
        context.setAttributeValue("uid", username);
        context.setAttributeValue("uidNumber", "10001");
        context.setAttributeValue("gidNumber", "5000");
        context.setAttributeValue("gecos", "Huang Kang");
        context.setAttributeValue("homeDirectory", "/home/hk");
        context.setAttributeValue("loginShell", "/bin/bash");
        context.setAttributeValue
                ("userPassword", password);

        template.bind(context);
        System.out.println("User " + username + " is created");
    }


    private LdapTemplate getTemplate() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://localhost:3389");
        contextSource.setBase("");
        contextSource.setUserDn("cn=admin,dc=dev,dc=ncl,dc=sg");
        contextSource.setPassword("...");

        try {
            contextSource.afterPropertiesSet();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        LdapTemplate template = new LdapTemplate();
        template.setContextSource(contextSource);
        return template;
    }

    public static void main(String[] args){
        LdapExample ldapClient = new LdapExample();

        // Get attribute information
        List<String> children = ldapClient.getListing();
        System.out.println("Attribute for current user is");
        for  (String child :children) {
            System.out.println(child);
        }

        // Create new user with same UID, SN, and DN
        ldapClient.createUser("hk", "...");

        // Get specific user based on the UID (not UID Number)
        List<String> users = ldapClient.getUser("hk");
        for  (String user :users) {
            System.out.println(user + " is exist");
        }

        // Delete user with specific UID
        ldapClient.deleteUser("hk");
    }
}