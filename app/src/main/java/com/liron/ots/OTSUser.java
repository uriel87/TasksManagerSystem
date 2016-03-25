package com.liron.ots;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("OTSUser")
public class OTSUser extends ParseObject
{
    public void setEmail(String email) { put("email", email); }
    public void setPhone(String phone) { put("phone", phone); }
    public void setTeamName(String teamName) { put("team", teamName); }
    public void setManagerName(String managerName) { put("manager_name", managerName); }
    public void setIsActive(boolean isActive) { put("is_active", isActive); }

    public String getEmail() { return getString("email"); }
    public String getPhone() { return getString("phone"); }
    public String getTeamName() { return getString("team"); }
    public String getManagerName() { return getString("manager_name"); }
    public boolean isActive() { return getBoolean("is_active"); }
}
