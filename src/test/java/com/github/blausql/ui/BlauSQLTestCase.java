package com.github.blausql.ui;

import com.github.blausql.TerminalUI;
import com.googlecode.lanterna.gui.Interactable;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.terminal.TerminalSize;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

import static org.easymock.EasyMock.*;

@PrepareForTest({com.github.blausql.Main.class, TerminalUI.class})
public class BlauSQLTestCase extends PowerMockTestCase {

    private static final TerminalSize DEFAULT_TERMINAL_SIZE = new TerminalSize(80, 24);


    @BeforeMethod
    public final void mockStaticCoreClasses() throws Exception {
        Method exitApplicationMethod =
                com.github.blausql.Main.class.getDeclaredMethod("exitApplication", int.class);

        PowerMock.mockStaticStrict(com.github.blausql.Main.class, exitApplicationMethod);
        PowerMock.mockStatic(TerminalUI.class);

        TerminalUI.init();
        EasyMock.expectLastCall().once();

        EasyMock.expect(TerminalUI.getTerminalSize()).andReturn(new TerminalSize(DEFAULT_TERMINAL_SIZE));
        EasyMock.expectLastCall().anyTimes();

    }

    protected static void replaySystemClasses() {
        PowerMock.replay(com.github.blausql.Main.class, TerminalUI.class);
    }

    protected static void verifyCoreClasses() {
        PowerMock.verify(com.github.blausql.Main.class, TerminalUI.class);
    }


    protected final void expectApplicationExitRequest(int expectedExitCode) {
        com.github.blausql.Main.exitApplication(expectedExitCode);
        EasyMock.expectLastCall().once();
    }

    protected static <T extends Window> Capture<T> expectWindowIsShownCenter(Class<T> windowClass) {
        Capture<T> capturedArgument = newCapture();

        TerminalUI.showWindowCenter(and(capture(capturedArgument), anyObject(windowClass)));
        EasyMock.expectLastCall().once();
        return capturedArgument;
    }

    protected static void expectErrorMessageBoxIsShownFrom(Throwable throwable) {
        TerminalUI.showErrorMessageFromThrowable(throwable);
        EasyMock.expectLastCall().once();
    }

    protected static void expectMessageBoxIsShown(String title, String text) {

        TerminalUI.showMessageBox(title, text);
        EasyMock.expectLastCall().once();
    }

    protected final void invokeMain() {
        com.github.blausql.Main.main(new String[] {});
    }

    protected static void pressEnterOn(Interactable interactableComponent) {
        interactableComponent.keyboardInteraction(new Key(Key.Kind.Enter));
    }


    protected <T extends Interactable> T getComponent(Object component, String fieldName) {
        return (T) ReflectionTestUtils.getField(component, fieldName);
    }
}
