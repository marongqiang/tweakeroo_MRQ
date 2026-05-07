package fi.dy.masa.tweakeroo.gui.layout;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.tweakeroo.Tweakeroo;

public class GuiStateManager
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File stateFile;
    private final Map<String, Boolean> expandedByGroupId = new HashMap<>();

    public GuiStateManager()
    {
        File configDir = new File(FileUtils.getConfigDirectory(), "tweakeroo");
        this.stateFile = new File(configDir, "gui_states.json");
        this.load();
    }

    public boolean isExpanded(String groupId, boolean defaultExpanded)
    {
        return this.expandedByGroupId.getOrDefault(groupId, defaultExpanded);
    }

    public void setExpanded(String groupId, boolean expanded)
    {
        this.expandedByGroupId.put(groupId, expanded);
        this.save();
    }

    public void toggle(String groupId, boolean defaultExpanded)
    {
        this.setExpanded(groupId, !this.isExpanded(groupId, defaultExpanded));
    }

    public void setAll(Map<String, Boolean> values)
    {
        // Merge update instead of clearing everything.
        // Clearing would wipe states for other tabs/groups and can cause confusing UX.
        this.expandedByGroupId.putAll(values);
        this.save();
    }

    private void load()
    {
        try
        {
            if (this.stateFile.exists() == false)
            {
                return;
            }

            String json = Files.readString(this.stateFile.toPath(), StandardCharsets.UTF_8);
            JsonObject obj = GSON.fromJson(json, JsonObject.class);

            if (obj == null)
            {
                return;
            }

            for (String key : obj.keySet())
            {
                this.expandedByGroupId.put(key, obj.get(key).getAsBoolean());
            }
        }
        catch (Exception e)
        {
            Tweakeroo.logger.warn("Failed to read gui state file {}", this.stateFile, e);
        }
    }

    private void save()
    {
        try
        {
            File parent = this.stateFile.getParentFile();
            if (parent.exists() == false)
            {
                parent.mkdirs();
            }

            JsonObject obj = new JsonObject();
            for (Map.Entry<String, Boolean> e : this.expandedByGroupId.entrySet())
            {
                obj.addProperty(e.getKey(), e.getValue());
            }

            Files.writeString(this.stateFile.toPath(), GSON.toJson(obj), StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            Tweakeroo.logger.warn("Failed to write gui state file {}", this.stateFile, e);
        }
    }
}

