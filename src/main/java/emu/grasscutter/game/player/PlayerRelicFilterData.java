package emu.grasscutter.game.player;

import dev.morphia.annotations.Entity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
public class PlayerRelicFilterData implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter @Setter private int avatarId;
    @Getter @Setter private List<Integer> relicSetIdList = new ArrayList<>();
    @Getter @Setter private List<SlotFilter> slotFilters = new ArrayList<>();

    public PlayerRelicFilterData() {}

    public PlayerRelicFilterData(int avatarId) {
        this.avatarId = avatarId;
    }

    @Entity
    public static class SlotFilter implements Serializable {
        private static final long serialVersionUID = 1L;

        @Getter @Setter private int equipType; // 1 to 5
        @Getter @Setter private int bmoichmncjc; // flag
        @Getter @Setter private List<Integer> affixList = new ArrayList<>();

        public SlotFilter() {}

        public SlotFilter(int equipType, int bmoichmncjc, List<Integer> affixList) {
            this.equipType = equipType;
            this.bmoichmncjc = bmoichmncjc;
            if (affixList != null) {
                this.affixList.addAll(affixList);
            }
        }
    }
}
