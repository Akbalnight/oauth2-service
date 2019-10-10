package com.assd.oauthservice.ldap;

import java.util.HashMap;
import java.util.Map;

public class LdapAttributesConst
{
    /**
     * Ключ - название LDAP атрибута
     * Значение - название переменной в БД
     */
    @SuppressWarnings("serial")
    public static final Map<String, String> ATTRIBUTES = new HashMap<String, String>()
    {
        {
            put("othertelephone", "otherTelephone");
            put("givenname", "firstName");
            put("telephonenumber", "telephoneNumber");
            put("displayname", "displayName");
            put("streetaddress", "streetAddress");
            put("name", "name");
            put("title", "title");
            put("department", "department");
            put("extensionattribute11", "extensionAttribute11");
            put("company", "company");
            put("mobile", "mobile");
            put("middlename", "middleName");
            put("sn", "lastName");
            put("mail", "mail");
        }
    };
    public static final String MAIL = "mail";
}
