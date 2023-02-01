package forge.ai.ability;

import forge.ai.ComputerUtil;
import forge.ai.ComputerUtilCard;
import forge.ai.SpellAbilityAi;
import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardLists;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.zone.ZoneType;

public class ConniveAi extends SpellAbilityAi {
    @Override
    protected boolean doTriggerAINoCost(Player ai, SpellAbility sa, boolean mandatory) {
        final Card source = sa.getHostCard();
        boolean preferred = true;
        CardCollection list;
        list = CardLists.getTargetableCards(new CardCollection(ai.getCardsIn(ZoneType.Battlefield)), sa);

        // Filter AI-specific targets if provided
        list = ComputerUtil.filterAITgts(sa, ai, list, false);

        int totalTargets = list.size();

        sa.resetTargets();
        while (sa.canAddMoreTarget()) {
            if (mandatory) {
                if ((list.isEmpty() || !preferred) && sa.isTargetNumberValid()) {
                    return true;
                }

                if (list.isEmpty() && preferred) {
                    // If it's required to choose targets and the list is empty, get a new list
                    list = CardLists.getTargetableCards(ai.getOpponents().getCardsIn(ZoneType.Battlefield), sa);
                    preferred = false;
                }

                if (list.isEmpty()) {
                    // Still an empty list, but we have to choose something (mandatory); expand targeting to
                    // include AI's own cards to see if there's anything targetable (e.g. Plague Belcher).
                    list = CardLists.getTargetableCards(ai.getCardsIn(ZoneType.Battlefield), sa);
                    preferred = false;
                }
            }

            if (list.isEmpty()) {
                // Not mandatory, or the the list was regenerated and is still empty,
                // so return whether or not we found enough targets
                return sa.isTargetNumberValid();
            }

            Card choice = ComputerUtilCard.getBestCreatureAI(list);

            if (choice != null) {
                sa.getTargets().add(choice);
                list.remove(choice);
            } else {
                // Didn't want to choose anything?
                list.clear();
            }
        }
        return true;
    }

}
