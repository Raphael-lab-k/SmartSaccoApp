package com.example.smartsaccoapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthManagerTest {

    @Mock
    Context mockContext;
    @Mock
    SharedPreferences mockPrefs;
    @Mock
    SharedPreferences.Editor mockEditor;

    private AuthManager authManager;

    @Before
    public void setUp() {
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);

        authManager = new AuthManager(mockContext);
    }

    @Test
    public void testSetLogin() {
        authManager.setLogin(true, "test@email.com", "Test User");

        verify(mockEditor).putBoolean("isLoggedIn", true);
        verify(mockEditor).putString("userEmail", "test@email.com");
        verify(mockEditor).putString("userName", "Test User");
        verify(mockEditor).apply();
    }

    @Test
    public void testIsLoggedIn() {
        when(mockPrefs.getBoolean("isLoggedIn", false)).thenReturn(true);
        assertTrue(authManager.isLoggedIn());
    }

    @Test
    public void testGetUserDetails() {
        when(mockPrefs.getString("userEmail", null)).thenReturn("test@email.com");
        when(mockPrefs.getString("userName", null)).thenReturn("Test User");

        assertEquals("test@email.com", authManager.getUserEmail());
        assertEquals("Test User", authManager.getUserName());
    }

    @Test
    public void testLogout() {
        authManager.logout();

        verify(mockEditor).clear();
        verify(mockEditor).apply();
    }
}