package fi.dy.masa.tweakeroo.gui.layout;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Logical group node used to flatten the JSON-driven layout into MaLiLib config list entries.
 * This is not a standalone render widget; it leverages MaLiLib's existing config list widgets.
 */
public class WidgetCollapsibleGroup
{
    public final String groupId;
    public final String name;
    public final boolean defaultExpanded;
    public final List<JsonElement> children;

    public WidgetCollapsibleGroup(String groupId, String name, boolean defaultExpanded, List<JsonElement> children)
    {
        this.groupId = groupId;
        this.name = name;
        this.defaultExpanded = defaultExpanded;
        this.children = children != null ? children : new ArrayList<>();
    }

    public static WidgetCollapsibleGroup fromJson(JsonObject obj)
    {
        String groupId = obj.has("groupId") ? obj.get("groupId").getAsString() : "";
        String name = obj.has("name") ? obj.get("name").getAsString() : groupId;
        boolean defaultExpanded = obj.has("defaultExpanded") && obj.get("defaultExpanded").getAsBoolean();

        List<JsonElement> children = new ArrayList<>();
        if (obj.has("children") && obj.get("children").isJsonArray())
        {
            obj.getAsJsonArray("children").forEach(children::add);
        }

        return new WidgetCollapsibleGroup(groupId, name, defaultExpanded, children);
    }
}

