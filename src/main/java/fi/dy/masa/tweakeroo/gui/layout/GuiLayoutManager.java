package fi.dy.masa.tweakeroo.gui.layout;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class GuiLayoutManager
{
    public static final String LAYOUT_REL_PATH = "tweakeroo/gui_layout.json";
    public static final String LAYOUT_RESOURCE_PATH = "assets/tweakeroo/gui_layout.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File layoutFile;
    private GuiLayout layout;

    public GuiLayoutManager()
    {
        this.layoutFile = new File(FileUtils.getConfigDirectory(), LAYOUT_REL_PATH);
        this.layout = this.load();
    }

    public GuiLayout getLayout()
    {
        return this.layout;
    }

    public List<TabDef> getTabs()
    {
        if (this.layout == null || this.layout.tabs == null)
        {
            return Collections.emptyList();
        }

        return this.layout.tabs;
    }

    public TabDef getTabById(String tabId)
    {
        for (TabDef tab : this.getTabs())
        {
            if (tab != null && tabId.equals(tab.tabId))
            {
                return tab;
            }
        }

        return null;
    }

    private GuiLayout load()
    {
        try
        {
            if (this.layoutFile.exists() == false)
            {
                // Create a sample layout file matching the supported schema.
                File parent = this.layoutFile.getParentFile();
                if (parent.exists() == false)
                {
                    parent.mkdirs();
                }
                // Try to copy from jar resource first, fallback to embedded default
                String defaultJson = loadDefaultLayoutFromResource();
                if (defaultJson != null)
                {
                    Files.writeString(this.layoutFile.toPath(), defaultJson, StandardCharsets.UTF_8);
                }
                else
                {
                    Files.writeString(this.layoutFile.toPath(), defaultLayoutJson(), StandardCharsets.UTF_8);
                }
            }

            String json = Files.readString(this.layoutFile.toPath(), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            GuiLayout loaded = new GuiLayout();

            if (root != null && root.has("tabs") && root.get("tabs").isJsonArray())
            {
                root.getAsJsonArray("tabs").forEach(el -> {
                    if (el != null && el.isJsonObject())
                    {
                        JsonObject t = el.getAsJsonObject();
                        TabDef tab = new TabDef();
                        tab.tabId = t.has("tabId") ? t.get("tabId").getAsString() : "";
                        tab.name = t.has("name") ? t.get("name").getAsString() : tab.tabId;
                        tab.groups = new ArrayList<>();
                        if (t.has("groups") && t.get("groups").isJsonArray())
                        {
                            t.getAsJsonArray("groups").forEach(tab.groups::add);
                        }
                        loaded.tabs.add(tab);
                    }
                });
            }

            return loaded;
        }
        catch (Exception e)
        {
            Tweakeroo.logger.warn("Failed to read gui layout file {}", this.layoutFile, e);
            return new GuiLayout();
        }
    }

    /**
     * Load the default layout from the jar resource file
     */
    private String loadDefaultLayoutFromResource()
    {
        try
        {
            InputStream is = Tweakeroo.class.getResourceAsStream("/" + LAYOUT_RESOURCE_PATH);
            if (is == null)
            {
                Tweakeroo.logger.warn("Default gui_layout.json not found in jar resources, using embedded fallback");
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    sb.append(line).append("\n");
                }
            }
            return sb.toString();
        }
        catch (Exception e)
        {
            Tweakeroo.logger.warn("Failed to load default gui_layout.json from jar resources", e);
            return null;
        }
    }

    private static String defaultLayoutJson()
    {
        // Minimal example using real config names as returned by IConfigBase#getName() (modid:snakeCaseName).
        // Wrong names produce no rows (only group headers).
        return """
        {
          "tabs": [
            {
              "tabId": "generic",
              "name": "Generic",
              "groups": [
                {
                  "groupId": "tweaks",
                  "name": "Tweaks",
                  "defaultExpanded": true,
                  "children": [
                    "tweakeroo:disableOffhandRendering",
                    "tweakeroo:tweakHotbarSwap"
                  ]
                },
                {
                  "groupId": "camera",
                  "name": "Camera",
                  "defaultExpanded": false,
                  "children": [
                    {
                      "groupId": "camera_angle",
                      "name": "Camera Angle",
                      "defaultExpanded": false,
                      "children": [
                        "tweakeroo:tweakFreeCamera",
                        "tweakeroo:tweakElytraCamera"
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
        """;
    }

    public static class GuiLayout
    {
        public List<TabDef> tabs = new ArrayList<>();
    }

    public static class TabDef
    {
        public String tabId;
        public String name;
        public List<JsonElement> groups;
    }
}

