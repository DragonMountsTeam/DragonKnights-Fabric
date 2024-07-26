package net.dragonmounts.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.*;
import net.minecraft.util.Language;

import java.util.List;
import java.util.Optional;

public class TextBlock {
    private final Text[] rawText;

    public TextBlock(Text... texts) {
        this.rawText = texts;
    }

    public void appendHoverText(List<Text> tooltips) {
        Text[] texts = this.rawText;
        MinecraftClient minecraft = MinecraftClient.getInstance();
        Screen screen = minecraft.currentScreen;
        if (screen != null) {
            Language language = Language.getInstance();
            Window window = minecraft.getWindow();
            TextRenderer renderer = minecraft.textRenderer;
            TextHandler handler = renderer.getTextHandler();
            int x = (int) (minecraft.mouse.getX() * window.getScaledWidth() / window.getWidth());
            int screenWidth = screen.width;
            int maxWidth = (x << 1) > screenWidth ? x - 20 : screenWidth - x - 16;
            for (Text text : texts) {
                int textWidth = renderer.getWidth(language.reorder(text));
                if (x + textWidth + 16 > screenWidth && x < 12 + textWidth) {
                    for (StringVisitable line : handler.wrapLines(text, maxWidth, text.getStyle())) {
                        tooltips.add(new TextImpl(line));
                    }
                } else {
                    tooltips.add(text.shallowCopy());
                }
            }
        } else for (Text text : texts) {
            tooltips.add(text.shallowCopy());
        }
    }

    public static class TextImpl implements Text {
        public final StringVisitable host;
        private Language priorLanguage;
        private OrderedText orderedText;

        protected TextImpl(StringVisitable host) {
            this.host = host;
        }

        @Override
        public Style getStyle() {
            return Style.EMPTY;
        }

        @Override
        public String asString() {
            return this.host.getString();
        }

        @Override
        public List<Text> getSiblings() {
            return ImmutableList.of();
        }

        @Override
        public MutableText copy() {
            return new LiteralText(this.host.getString());
        }

        @Override
        public MutableText shallowCopy() {
            return new LiteralText(this.host.getString());
        }

        @Override
        public OrderedText asOrderedText() {
            Language language = Language.getInstance();
            if (this.priorLanguage != language) {
                this.orderedText = language.reorder(this.host);
                this.priorLanguage = language;
            }
            return this.orderedText;
        }

        @Override
        public <T> Optional<T> visit(Visitor<T> visitor) {
            return this.host.visit(visitor);
        }

        @Override
        public <T> Optional<T> visit(StyledVisitor<T> visitor, Style style) {
            return this.host.visit(visitor, style);
        }
    }
}
