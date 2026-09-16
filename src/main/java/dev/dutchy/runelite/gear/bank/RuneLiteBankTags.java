package dev.dutchy.runelite.gear.bank;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.banktags.BankTagsService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/** The core Bank Tags plugin's own state, read through its service; closing a tag rebuilds the bank, so it goes to the client thread. */
@Singleton
public final class RuneLiteBankTags implements BankTagState {

    private final BankTagsService tags;
    private final ClientThread clientThread;

    @Inject
    public RuneLiteBankTags(BankTagsService tags, ClientThread clientThread) {
        this.tags = Objects.requireNonNull(tags, "tags");
        this.clientThread = Objects.requireNonNull(clientThread, "clientThread");
    }

    @Override
    public boolean isTagOpen() {
        return tags.getActiveBankTag() != null;
    }

    @Override
    public void closeTag() {
        clientThread.invoke(tags::closeBankTag);
    }
}
