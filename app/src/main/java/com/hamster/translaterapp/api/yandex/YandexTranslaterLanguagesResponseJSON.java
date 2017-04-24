
package com.hamster.translaterapp.api.yandex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class YandexTranslaterLanguagesResponseJSON {

    @SerializedName("dirs")
    @Expose
    private List<Object> dirs = null;
    @SerializedName("langs")
    @Expose
    private Map<String, String> langs = new HashMap<String, String>();

    public List<Object> getDirs() {
        return dirs;
    }

    public void setDirs(List<Object> dirs) {
        this.dirs = dirs;
    }

    public Map<String, String> getLangs() {
        return this.langs;
    }

    public void setLangs(String name, String value) {
        this.langs.put(name, value);
    }

}
