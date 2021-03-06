/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.plugins.github.ui;

import com.intellij.ide.BrowserUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;

/**
 * @author oleg
 * @date 10/20/10
 */
public class GithubLoginPanel {
  private JPanel myPane;
  private JTextField myHostTextField;
  private JTextField myLoginTextField;
  private JPasswordField myPasswordField;
  private JTextPane mySignupTextField;

  public GithubLoginPanel(final GithubLoginDialog dialog) {
    DocumentListener listener = new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        dialog.clearErrors();
      }
    };
    myLoginTextField.getDocument().addDocumentListener(listener);
    myPasswordField.getDocument().addDocumentListener(listener);
    mySignupTextField.setText("<html>Do not have an account at github.com? <a href=\"https://github.com\">Sign up</a>.</html>");
    mySignupTextField.setMargin(new Insets(5, 0, 0, 0));
    mySignupTextField.addHyperlinkListener(new HyperlinkAdapter() {
      @Override
      protected void hyperlinkActivated(final HyperlinkEvent e) {
        BrowserUtil.browse(e.getURL());
      }
    });
    mySignupTextField.setBackground(UIUtil.TRANSPARENT_COLOR);
    mySignupTextField.setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  public JComponent getPanel() {
    return myPane;
  }

  public void setHost(final String host) {
    myHostTextField.setText(host);
  }

  public void setLogin(final String login) {
    myLoginTextField.setText(login);
  }

  public void setPassword(final String password) {
    myPasswordField.setText(password);
  }

  public String getHost() {
    return myHostTextField.getText().trim();
  }

  public String getLogin() {
    return myLoginTextField.getText().trim();
  }

  public String getPassword() {
    return String.valueOf(myPasswordField.getPassword());
  }

  public JComponent getPreferrableFocusComponent() {
    return myLoginTextField;
  }
}

