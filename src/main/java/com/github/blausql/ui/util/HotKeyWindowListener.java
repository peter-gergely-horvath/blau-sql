package com.github.blausql.ui.util;

import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public final class HotKeyWindowListener extends WindowListenerAdapter {

    private static class Rule {
        private final KeyType keyType;
        private final Character character;
        private final boolean ctrlDown;
        private final boolean altDown;
        private final boolean shiftDown;

        private final Runnable action;

        private Rule(KeyType keyType,
                     Character character,
                     boolean ctrlDown,
                     boolean altDown,
                     boolean shiftDown,
                     Runnable action) {
            this.keyType = keyType;
            this.character = character;
            this.ctrlDown = ctrlDown;
            this.altDown = altDown;
            this.shiftDown = shiftDown;
            this.action = action;
        }

        boolean marches(KeyStroke keyStroke) {
            if (keyStroke != null) {
                if (this.keyType == keyStroke.getKeyType()
                        && charactersMatch(this.character, keyStroke.getCharacter())
                        && this.ctrlDown == keyStroke.isCtrlDown()
                        && this.altDown == keyStroke.isAltDown()
                        && this.shiftDown == keyStroke.isShiftDown()) {

                    return true;
                }
            }

            return false;
        }

        private static boolean charactersMatch(Character character1, Character character2) {

            return Objects.equals(
                    normalizeCharacter(character1),
                    normalizeCharacter(character2));
        }

        private static Character normalizeCharacter(Character character1) {
            return Optional.ofNullable(character1).map(Character::toUpperCase).orElse(null);
        }

        public void apply() {
            action.run();
        }
    }


    public interface KeyDefinitionConfiguration {
        ActionConfigurable keyType(KeyType keyType);
        ActionConfigurable character(Character character);
        ActionConfigurable ctrlDown();
        ActionConfigurable altDown(boolean altDown);
        ActionConfigurable shiftDown();
    }

    public interface ActionConfigurable extends KeyDefinitionConfiguration {
        Buildable invoke(Runnable runnable);
    }

    public interface Buildable extends KeyDefinitionConfiguration {
        HotKeyWindowListener build();
    }


    public static final class Builder implements KeyDefinitionConfiguration, ActionConfigurable, Buildable {

        private KeyType keyType;
        private Character character;
        private boolean ctrlDown;
        private boolean altDown;
        private boolean shiftDown;


        private final List<Rule> rules = new LinkedList<>();


        @Override
        public ActionConfigurable keyType(KeyType keyType) {
            this.keyType = keyType;

            return this;
        }

        @Override
        public ActionConfigurable character(Character character) {
            this.keyType = KeyType.Character;
            this.character = character;

            return this;
        }

        @Override
        public ActionConfigurable ctrlDown() {
            this.ctrlDown = true;

            return this;
        }

        @Override
        public ActionConfigurable altDown(boolean altDown) {
            this.altDown = true;

            return this;
        }

        @Override
        public ActionConfigurable shiftDown() {
            this.shiftDown = true;

            return this;
        }

        @Override
        public Buildable invoke(Runnable runnable) {

            Rule rule = new Rule(keyType,
                    character,
                    ctrlDown,
                    altDown,
                    shiftDown,
                    runnable);

            rules.add(rule);

            keyType = null;
            character  = null;
            ctrlDown = false;
            altDown  = false;
            shiftDown = false;

            return this;
        }

        @Override
        public HotKeyWindowListener build() {
            return new HotKeyWindowListener(rules);
        }


    }

    public static Builder builder() {
        return new Builder();
    }


    private final List<Rule> rules;

    private HotKeyWindowListener(List<Rule> rules) {
        this.rules = rules;
    }

    public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {

        for (Rule rule : rules) {
            if (rule.marches(keyStroke)) {

                try {
                    rule.apply();

                } finally {
                    hasBeenHandled.set(true);
                }

                break;
            }
        }
    }
}

