package net.lunovastudio.lobotomycorp.api.research;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 研究分类。在本模组里分类与“部门”一一对应：分类是研究界面里的一栏，也是知识点的累计单位。
 *
 * <p>分类从 {@link #REGISTRY_KEY} 数据包注册表加载，路径为
 * {@code data/<namespace>/lobotomycorp/research_category/}。</p>
 */
public interface IResearchCategory {
    /** 研究分类的数据包注册表键。 */
    ResourceKey<Registry<IResearchCategory>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("lobotomycorp", "research_category"));

    /** 分类显示名的翻译键。 */
    String nameKey();

    /** 分类描述的翻译键。 */
    String descriptionKey();

    /**
     * 解锁该分类所需完成的研究条目；为空表示无条件可见。
     */
    Optional<ResourceLocation> requiredResearch();

    /**
     * 把扫描到的知识权重换算成分类知识点的公式权重表。
     *
     * <p>这是为本项目后续自建 aspect 体系预留的接入点：键是外部体系的 id，值是该 id 的权重。
     * 目前没有任何代码会写入这张表。</p>
     */
    Map<ResourceLocation, Integer> formula();

    /**
     * 用本分类的 {@link #formula() 公式} 把一组权重换算成分类知识点。
     * 每个公式项按“扫描量 × 权重/10”累加，再开平方后向上取整。
     *
     * @param scanned 外部体系给出的权重表
     * @return 获得的原始知识点，非负
     */
    default int applyFormula(Map<ResourceLocation, Integer> scanned) {
        return applyFormula(scanned, 1.0);
    }

    /**
     * 带倍率的 {@link #applyFormula(Map)}。倍率在加权前平方，因此缩放的是开方前的和。
     */
    default int applyFormula(Map<ResourceLocation, Integer> scanned, double modifier) {
        double total = 0.0;
        for (Map.Entry<ResourceLocation, Integer> entry : formula().entrySet()) {
            total += modifier * modifier
                    * scanned.getOrDefault(entry.getKey(), 0)
                    * (entry.getValue() / 10.0);
        }
        if (total > 0.0) {
            total = Math.sqrt(total);
        }
        return Mth.ceil(total);
    }

    /** 分类标签的贴图 id。 */
    ResourceLocation icon();

    /** 分类面板背景贴图 id。 */
    Optional<ResourceLocation> background();

    /** 叠加在背景之上的第二层背景贴图 id。 */
    Optional<ResourceLocation> overlayBackground();

    /** 分类主题色（ARGB）。 */
    int color();

    /** 分类排序索引，越小越先显示。 */
    int index();

    /** 访问该分类需要的员工等级。 */
    int requiredEmployeeGrade();

    /** 首次获得访问权时默认的部门权限等级。 */
    int defaultAccessLevel();

    /** 生命树布局：节点归一化横坐标。 */
    double treeX();

    /** 生命树布局：节点归一化纵坐标。 */
    double treeY();

    /** 生命树布局：与之相连的其他分类 id。 */
    List<ResourceLocation> treeConnections();
}
