package net.lunovastudio.lobotomycorp.content.research;

import net.minecraft.nbt.NbtOps;
import net.lunovastudio.lobotomycorp.api.capability.ResearchFlag;
import net.lunovastudio.lobotomycorp.api.capability.ResearchStatus;
import net.lunovastudio.lobotomycorp.api.research.knowledge.KnowledgeType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PlayerKnowledge} 的基础语义与编解码往返测试。
 */
class PlayerKnowledgeTest {
    private static final ResourceLocation ENTRY = ResourceLocation.fromNamespaceAndPath("lobotomycorp", "control_test_1");
    private static final ResourceLocation OTHER = ResourceLocation.fromNamespaceAndPath("lobotomycorp", "control_test_2");

    @Test
    void unknownEntriesReportUnknownStatus() {
        PlayerKnowledge knowledge = new PlayerKnowledge();

        assertEquals(ResearchStatus.UNKNOWN, knowledge.status(ENTRY));
        assertFalse(knowledge.knows(ENTRY));
        assertEquals(-1, knowledge.stageOf(ENTRY));
    }

    @Test
    void learningAndCompletingUpdatesStatus() {
        PlayerKnowledge knowledge = new PlayerKnowledge();

        assertTrue(knowledge.learn(ENTRY));
        assertFalse(knowledge.learn(ENTRY));
        assertEquals(ResearchStatus.IN_PROGRESS, knowledge.status(ENTRY));
        assertEquals(0, knowledge.stageOf(ENTRY));

        assertTrue(knowledge.completeEntry(ENTRY));
        assertFalse(knowledge.completeEntry(ENTRY));
        assertEquals(ResearchStatus.COMPLETE, knowledge.status(ENTRY));
        assertTrue(knowledge.hasReachedStage(ENTRY, 3));
    }

    @Test
    void stageChangesRequireKnownEntry() {
        PlayerKnowledge knowledge = new PlayerKnowledge();

        assertFalse(knowledge.setStage(ENTRY, 1));
        knowledge.learn(ENTRY);
        assertFalse(knowledge.setStage(ENTRY, 0));
        assertTrue(knowledge.setStage(ENTRY, 2));
        assertFalse(knowledge.setStage(ENTRY, 2));
        assertEquals(2, knowledge.stageOf(ENTRY));
    }

    @Test
    void forgettingClearsEveryField() {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        knowledge.learn(ENTRY);
        knowledge.setStage(ENTRY, 1);
        knowledge.addFlag(ENTRY, ResearchFlag.PAGE);

        assertTrue(knowledge.forget(ENTRY));
        assertFalse(knowledge.forget(ENTRY));
        assertFalse(knowledge.knows(ENTRY));
        assertFalse(knowledge.hasFlag(ENTRY, ResearchFlag.PAGE));
        assertEquals(-1, knowledge.stageOf(ENTRY));
    }

    @Test
    void flagsAreIndependentOfCompletion() {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        knowledge.learn(ENTRY);

        assertTrue(knowledge.addFlag(ENTRY, ResearchFlag.RESEARCH));
        assertFalse(knowledge.addFlag(ENTRY, ResearchFlag.RESEARCH));
        assertTrue(knowledge.hasFlag(ENTRY, ResearchFlag.RESEARCH));
        assertTrue(knowledge.removeFlag(ENTRY, ResearchFlag.RESEARCH));
        assertFalse(knowledge.hasFlag(ENTRY, ResearchFlag.RESEARCH));
    }

    @Test
    void knowledgeNeverDropsBelowZero() {
        PlayerKnowledge knowledge = new PlayerKnowledge();

        assertTrue(knowledge.addKnowledgePoints(KnowledgeType.OBSERVATION, null, 40));
        assertEquals(40, knowledge.knowledgePoints(KnowledgeType.OBSERVATION, null));
        assertEquals(2, knowledge.knowledgeLevel(KnowledgeType.OBSERVATION, null));

        assertTrue(knowledge.addKnowledgePoints(KnowledgeType.OBSERVATION, null, -999));
        assertEquals(0, knowledge.knowledgePoints(KnowledgeType.OBSERVATION, null));
        assertFalse(knowledge.addKnowledgePoints(KnowledgeType.OBSERVATION, null, -1));
    }

    @Test
    void codecRoundTripsProgress() {
        PlayerKnowledge knowledge = new PlayerKnowledge();
        knowledge.learn(ENTRY);
        knowledge.setStage(ENTRY, 2);
        knowledge.addFlag(ENTRY, ResearchFlag.PAGE);
        knowledge.learn(OTHER);
        knowledge.completeEntry(OTHER);
        knowledge.addKnowledgePoints(KnowledgeType.THEORY, null, 12);

        var encoded = PlayerKnowledge.CODEC.codec()
                .encodeStart(NbtOps.INSTANCE, knowledge)
                .getOrThrow(error -> new IllegalStateException(error));
        PlayerKnowledge decoded = PlayerKnowledge.CODEC.codec()
                .parse(NbtOps.INSTANCE, encoded)
                .getOrThrow(error -> new IllegalStateException(error));

        assertTrue(decoded.knows(ENTRY));
        assertTrue(decoded.hasFlag(ENTRY, ResearchFlag.PAGE));
        assertEquals(2, decoded.stageOf(ENTRY));
        assertTrue(decoded.hasCompleted(OTHER));
        assertEquals(12, decoded.knowledgePoints(KnowledgeType.THEORY, null));
    }
}
