package fi.dy.masa.tweakeroo.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IHotkeyTogglable;
import fi.dy.masa.malilib.config.options.BooleanHotkeyGuiWrapper;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IConfigGroupHandler;
import fi.dy.masa.malilib.util.StringUtils;
import fi.dy.masa.tweakeroo.Reference;
import fi.dy.masa.tweakeroo.config.Configs;
import fi.dy.masa.tweakeroo.config.FeatureToggle;
import fi.dy.masa.tweakeroo.config.Hotkeys;
import fi.dy.masa.tweakeroo.gui.layout.GuiLayoutManager;
import fi.dy.masa.tweakeroo.gui.layout.GuiLayoutManager.TabDef;
import fi.dy.masa.tweakeroo.gui.layout.GuiStateManager;
import fi.dy.masa.tweakeroo.gui.layout.WidgetCollapsibleGroup;

/**
 * JSON-driven configuration GUI layout for Tweakeroo.
 *
 * Layout file: config/tweakeroo/gui_layout.json
 * State file:  config/tweakeroo/gui_states.json
 */
public class GuiConfigs extends GuiConfigsBase implements IConfigGroupHandler
{
    // Keep the existing config entities unchanged; these lists are still the source of truth.
    public static ImmutableList<FeatureToggle> TWEAK_LIST = FeatureToggle.VALUES;
    public static ImmutableList<IHotkeyTogglable> YEET_LIST = Configs.Disable.OPTIONS;

    private static final String GROUP_LABEL_PREFIX = "\u0001group:";
    private static final GuiLayoutManager LAYOUTS = new GuiLayoutManager();
    private static final GuiStateManager STATES = new GuiStateManager();

    private static String activeTabId = "";

    private final Map<String, IConfigBase> configIndex = new HashMap<>();
    private final Map<String, Boolean> groupDefaultExpanded = new HashMap<>();

    public GuiConfigs()
    {
        super(10, 50, Reference.MOD_ID, null, "tweakeroo.gui.title.configs", String.format("%s", Reference.MOD_VERSION));
        this.buildConfigIndex();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.clearOptions();

        int x = 10;
        int y = 26;

        List<TabDef> tabs = LAYOUTS.getTabs();

        if (tabs.isEmpty() == false && (activeTabId == null || activeTabId.isBlank()))
        {
            activeTabId = tabs.get(0).tabId;
        }

        for (TabDef tab : tabs)
        {
            x += this.createTabButton(x, y, -1, tab);
        }

        // Expand / collapse all buttons at top-right
        int right = this.width - 10;
        ButtonGeneric collapseAll = new ButtonGeneric(right - 90, y, 44, 20, "▸▸");
        ButtonGeneric expandAll = new ButtonGeneric(right - 44, y, 44, 20, "▾▾");
        this.addButton(collapseAll, new IButtonActionListener()
        {
            @Override
            public void actionPerformedWithButton(ButtonBase button, int mouseButton)
            {
                setAllGroupsExpanded(false);
                getListWidget().refreshEntries();
                getListWidget().resetScrollbarPosition();
            }
        });
        this.addButton(expandAll, new IButtonActionListener()
        {
            @Override
            public void actionPerformedWithButton(ButtonBase button, int mouseButton)
            {
                setAllGroupsExpanded(true);
                getListWidget().refreshEntries();
                getListWidget().resetScrollbarPosition();
            }
        });
    }

    private int createTabButton(int x, int y, int width, TabDef tab)
    {
        String name = tab.name != null ? tab.name : tab.tabId;
        // If the JSON provides a translation key, translate it (allows zh/en etc).
        if (name.indexOf('.') >= 0)
        {
            name = StringUtils.translate(name);
        }
        ButtonGeneric button = new ButtonGeneric(x, y, width, 20, name);
        button.setEnabled(activeTabId.equals(tab.tabId) == false);
        this.addButton(button, new ButtonListener(tab.tabId, this));

        return button.getWidth() + 2;
    }

    @Override
    protected int getConfigWidth()
    {
        // Keep existing default width; layout file can optionally be extended later.
        return 260;
    }

    @Override
    protected boolean useKeybindSearch()
    {
        // Always allow searching; group headers will be shown for matches via MaLiLib filter enhancements.
        return true;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs()
    {
        List<TabDef> allTabs = LAYOUTS.getTabs();
        if (allTabs.isEmpty() == false)
        {
            if (activeTabId == null || activeTabId.isBlank() || LAYOUTS.getTabById(activeTabId) == null)
            {
                activeTabId = allTabs.get(0).tabId;
            }
        }

        TabDef tab = LAYOUTS.getTabById(activeTabId);

        if (tab == null || tab.groups == null)
        {
            return Collections.emptyList();
        }

        this.groupDefaultExpanded.clear();
        List<ConfigOptionWrapper> out = new ArrayList<>();

        for (JsonElement el : tab.groups)
        {
            this.flattenGroupOrConfig(out, activeTabId, el, 0);
        }

        return out;
    }

    private void flattenGroupOrConfig(List<ConfigOptionWrapper> out, String tabId, JsonElement el, int indent)
    {
        if (el == null)
        {
            return;
        }

        if (el.isJsonPrimitive())
        {
            String id = el.getAsString();
            IConfigBase cfg = this.resolveConfig(id);

            if (cfg != null)
            {
                out.add(new ConfigOptionWrapper(cfg, indent));
            }

            return;
        }

        if (el.isJsonObject())
        {
            JsonObject obj = el.getAsJsonObject();
            WidgetCollapsibleGroup group = WidgetCollapsibleGroup.fromJson(obj);
            String groupId = tabId + "/" + group.groupId;
            this.groupDefaultExpanded.put(groupId, group.defaultExpanded);
            String displayName = group.name;

            // If the group name matches a real config id (eg. "tweakMirror"), show the translated config name.
            // This is what makes top-level tweak groups appear in Chinese instead of raw internal IDs.
            IConfigBase maybeConfig = this.resolveConfig(Reference.MOD_ID + ":" + group.name);
            if (maybeConfig != null)
            {
                displayName = maybeConfig.getConfigGuiDisplayName();
            }
            else if (displayName != null && displayName.indexOf('.') >= 0)
            {
                // Allow using translation keys in JSON group names too
                displayName = StringUtils.translate(displayName);
            }

            out.add(new ConfigOptionWrapper(GROUP_LABEL_PREFIX + groupId + "|" + indent + "|" + displayName, indent));

            for (JsonElement child : group.children)
            {
                this.flattenGroupOrConfig(out, tabId, child, indent + 1);
            }
        }
    }

    @Override
    public boolean isGroupExpanded(String groupId)
    {
        boolean def = this.groupDefaultExpanded.getOrDefault(groupId, true);
        return STATES.isExpanded(groupId, def);
    }

    @Override
    public void toggleGroupExpanded(String groupId)
    {
        boolean def = this.groupDefaultExpanded.getOrDefault(groupId, true);
        STATES.toggle(groupId, def);
    }

    private void setAllGroupsExpanded(boolean expanded)
    {
        TabDef tab = LAYOUTS.getTabById(activeTabId);
        if (tab == null || tab.groups == null)
        {
            return;
        }

        Set<String> groupIds = new LinkedHashSet<>();
        collectGroupIds(groupIds, activeTabId, tab.groups);

        Map<String, Boolean> map = new HashMap<>();
        for (String id : groupIds)
        {
            map.put(id, expanded);
        }

        STATES.setAll(map);
    }

    private void collectGroupIds(Set<String> out, String tabId, List<JsonElement> groups)
    {
        for (JsonElement el : groups)
        {
            if (el != null && el.isJsonObject())
            {
                WidgetCollapsibleGroup group = WidgetCollapsibleGroup.fromJson(el.getAsJsonObject());
                String id = tabId + "/" + group.groupId;
                out.add(id);
                for (JsonElement child : group.children)
                {
                    if (child != null && child.isJsonObject())
                    {
                        collectGroupIds(out, tabId, Collections.singletonList(child));
                    }
                }
            }
        }
    }

    protected BooleanHotkeyGuiWrapper wrapConfig(FeatureToggle config)
    {
        return new BooleanHotkeyGuiWrapper(config.getName(), config, config.getKeybind());
    }

    private void buildConfigIndex()
    {
        this.configIndex.clear();

        for (IConfigBase cfg : Configs.Generic.OPTIONS) this.index(cfg);
        for (IConfigBase cfg : Configs.Fixes.OPTIONS) this.index(cfg);
        for (IConfigBase cfg : Configs.Lists.OPTIONS) this.index(cfg);
        for (IConfigBase cfg : Hotkeys.HOTKEY_LIST) this.index(cfg);
        for (IHotkeyTogglable cfg : YEET_LIST) this.index((IConfigBase) cfg);

        for (FeatureToggle t : TWEAK_LIST)
        {
            this.index(this.wrapConfig(t));
        }
    }

    private void index(IConfigBase cfg)
    {
        String key = (Reference.MOD_ID + ":" + cfg.getName()).toLowerCase();
        this.configIndex.put(key, cfg);
    }

    private IConfigBase resolveConfig(String id)
    {
        if (id == null)
        {
            return null;
        }

        String key = id.toLowerCase();
        if (key.contains(":") == false)
        {
            key = Reference.MOD_ID + ":" + key;
        }

        return this.configIndex.get(key);
    }

    private static class ButtonListener implements IButtonActionListener
    {
        private final GuiConfigs parent;
        private final String tabId;

        public ButtonListener(String tabId, GuiConfigs parent)
        {
            this.tabId = tabId;
            this.parent = parent;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            activeTabId = this.tabId;
            this.parent.reCreateListWidget();
            this.parent.getListWidget().resetScrollbarPosition();
            this.parent.initGui();
        }
    }
}
