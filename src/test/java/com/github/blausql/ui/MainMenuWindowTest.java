/*
 * Copyright (c) 2017 Peter G. Horvath, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.github.blausql.ui;

import com.github.blausql.TerminalUI;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static org.easymock.EasyMock.*;

@PrepareForTest({com.github.blausql.Main.class, TerminalUI.class})
public class MainMenuWindowTest extends PowerMockTestCase {


    @Test
    public void testQuitButton() throws NoSuchMethodException {

        Method exitApplicationMethod = com.github.blausql.Main.class.getDeclaredMethod("exitApplication", int.class);

        PowerMock.mockStaticStrict(com.github.blausql.Main.class, exitApplicationMethod);
        PowerMock.mockStatic(TerminalUI.class);


        Capture<MainMenuWindow> capturedArgument = newCapture();

        TerminalUI.init();
        EasyMock.expectLastCall().once();

        TerminalUI.showWindowCenter(and(capture(capturedArgument), anyObject(MainMenuWindow.class)));
        EasyMock.expectLastCall().once();


        com.github.blausql.Main.exitApplication(0);
        EasyMock.expectLastCall().once();

        PowerMock.replay(com.github.blausql.Main.class, TerminalUI.class);

        com.github.blausql.Main.main(new String[] {});

        MainMenuWindow mainMenuWindow = capturedArgument.getValue();
        Assert.assertNotNull(mainMenuWindow);

        mainMenuWindow.onQuitApplicationButtonSelected.doAction();


        PowerMock.verify(com.github.blausql.Main.class, TerminalUI.class);






    }



}
