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

import com.github.blausql.core.Constants;
import com.github.blausql.core.preferences.ConfigurationRepository;
import com.github.blausql.core.preferences.LoadException;
import com.googlecode.lanterna.gui.component.EditArea;
import org.easymock.Capture;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.easymock.EasyMock.*;

@PrepareForTest({ConfigurationRepository.class, ConfigurationRepository.class})
public class MainMenuWindowTest extends BlauSQLTestCaseBase {


    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Test
    public void testQuit() {

        Capture<MainMenuWindow> mainMenuWindowCapture = expectWindowIsShownCenter(MainMenuWindow.class);

        expectApplicationExitRequest(0);

        // --- REPLAY
        replaySystemClasses();

        invokeMain();

        MainMenuWindow mainMenuWindow = mainMenuWindowCapture.getValue();
        assertNotNull(mainMenuWindow);

        pressEnterOn(getComponent(mainMenuWindow, "quitApplicationButton"));

        // --- VERIFY
        verifyCoreClasses();
    }

    @Test
    public void testAbout() {

        Capture<MainMenuWindow> mainMenuWindowCapture = expectWindowIsShownCenter(MainMenuWindow.class);

        expectMessageBoxIsShown("About BlauSQL", Constants.ABOUT_TEXT);

        // --- REPLAY
        replaySystemClasses();

        invokeMain();

        MainMenuWindow mainMenuWindow = mainMenuWindowCapture.getValue();
        assertNotNull(mainMenuWindow);

        pressEnterOn(getComponent(mainMenuWindow, "aboutButton"));

        // --- VERIFY
        verifyCoreClasses();
    }

    @Test
    public void testSetApplicationClasspathNormalCase() throws LoadException {

        PowerMock.mockStatic(ConfigurationRepository.class);

        ConfigurationRepository mockConfigurationRepository = createMock(ConfigurationRepository.class);
        expect(ConfigurationRepository.getInstance()).andReturn(mockConfigurationRepository);


        expect(mockConfigurationRepository.getClasspath())
                .andReturn(new String[]{ "foo", "bar" });


        Capture<MainMenuWindow> capturedMainMenuWindow = expectWindowIsShownCenter(MainMenuWindow.class);

        Capture<SetClasspathWindow> capturedSetClasspathWindow = expectWindowIsShownCenter(SetClasspathWindow.class);


        // --- REPLAY
        PowerMock.replay(ConfigurationRepository.class);
        replay(mockConfigurationRepository);
        replaySystemClasses();
        replay();

        invokeMain();

        MainMenuWindow mainMenuWindow = capturedMainMenuWindow.getValue();
        assertNotNull(mainMenuWindow);


        pressEnterOn(getComponent(mainMenuWindow, "setApplicationClasspathButton"));


        SetClasspathWindow setClasspathWindow = capturedSetClasspathWindow.getValue();
        assertNotNull(setClasspathWindow);

        EditArea classpathEditArea = getComponent(setClasspathWindow, "classpathEditArea");
        String classPathEntries = classpathEditArea.getData();

        String expected = String.format("foo%sbar%s", LINE_SEPARATOR, LINE_SEPARATOR);
        assertEquals(classPathEntries, expected);

        // --- VERIFY
        verifyCoreClasses();
        PowerMock.verify(ConfigurationRepository.class);
        verify(mockConfigurationRepository);
    }

    @Test
    public void testSetApplicationClasspathEmpty() throws LoadException {

        PowerMock.mockStatic(ConfigurationRepository.class);

        ConfigurationRepository mockConfigurationRepository = createMock(ConfigurationRepository.class);
        expect(ConfigurationRepository.getInstance()).andReturn(mockConfigurationRepository);


        expect(mockConfigurationRepository.getClasspath())
                .andReturn(new String[0]);


        Capture<MainMenuWindow> capturedMainMenuWindow = expectWindowIsShownCenter(MainMenuWindow.class);

        Capture<SetClasspathWindow> capturedSetClasspathWindow = expectWindowIsShownCenter(SetClasspathWindow.class);


        // --- REPLAY
        PowerMock.replay(ConfigurationRepository.class);
        replay(mockConfigurationRepository);
        replaySystemClasses();
        replay();

        invokeMain();

        MainMenuWindow mainMenuWindow = capturedMainMenuWindow.getValue();
        assertNotNull(mainMenuWindow);


        pressEnterOn(getComponent(mainMenuWindow, "setApplicationClasspathButton"));


        SetClasspathWindow setClasspathWindow = capturedSetClasspathWindow.getValue();
        assertNotNull(setClasspathWindow);

        EditArea classpathEditArea = getComponent(setClasspathWindow, "classpathEditArea");
        String classPathEntries = classpathEditArea.getData();

        assertEquals(classPathEntries, "");

        // --- VERIFY
        verifyCoreClasses();
        PowerMock.verify(ConfigurationRepository.class);
        verify(mockConfigurationRepository);
    }

    @Test
    public void testSetApplicationClasspathLoadThrowsException() throws LoadException {

        PowerMock.mockStatic(ConfigurationRepository.class);

        ConfigurationRepository mockConfigurationRepository = createMock(ConfigurationRepository.class);
        expect(ConfigurationRepository.getInstance()).andReturn(mockConfigurationRepository);


        RuntimeException dummyException = new RuntimeException("Failed to fetch classpath repository");
        expect(mockConfigurationRepository.getClasspath())
                .andThrow(dummyException);


        Capture<MainMenuWindow> capturedMainMenuWindow = expectWindowIsShownCenter(MainMenuWindow.class);

        expectErrorMessageBoxIsShownFrom(dummyException);

        // --- REPLAY
        PowerMock.replay(ConfigurationRepository.class);
        replay(mockConfigurationRepository);
        replaySystemClasses();
        replay();

        invokeMain();

        MainMenuWindow mainMenuWindow = capturedMainMenuWindow.getValue();
        assertNotNull(mainMenuWindow);


        pressEnterOn(getComponent(mainMenuWindow, "setApplicationClasspathButton"));

        // --- VERIFY
        verifyCoreClasses();
        PowerMock.verify(ConfigurationRepository.class);
        verify(mockConfigurationRepository);
    }


}
