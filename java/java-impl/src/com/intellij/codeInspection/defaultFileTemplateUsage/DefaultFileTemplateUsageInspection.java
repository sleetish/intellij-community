/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package com.intellij.codeInspection.defaultFileTemplateUsage;

import com.intellij.codeInspection.*;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.impl.FileTemplateConfigurable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author cdr
 */
public class DefaultFileTemplateUsageInspection extends BaseJavaLocalInspectionTool {
  public boolean CHECK_FILE_HEADER = true;
  public boolean CHECK_TRY_CATCH_SECTION = true;
  public boolean CHECK_METHOD_BODY = true;

  @Override
  @NotNull
  public String getGroupDisplayName() {
    return GENERAL_GROUP_NAME;
  }

  @Override
  @NotNull
  public String getDisplayName() {
    return InspectionsBundle.message("default.file.template.display.name");
  }

  @Override
  @NotNull
  @NonNls
  public String getShortName() {
    return "DefaultFileTemplate";
  }

  @Override
  @Nullable
  public ProblemDescriptor[] checkMethod(@NotNull PsiMethod method, @NotNull InspectionManager manager, boolean isOnTheFly) {
    Collection<ProblemDescriptor> descriptors = new ArrayList<ProblemDescriptor>();
    if (CHECK_METHOD_BODY) {
      MethodBodyChecker.checkMethodBody(method, manager, descriptors, isOnTheFly);
    }
    if (CHECK_TRY_CATCH_SECTION) {
      CatchBodyVisitor visitor = new CatchBodyVisitor(manager, descriptors, isOnTheFly);
      PsiCodeBlock body = method.getBody();
      if (body != null) {
        body.accept(visitor);
      }
    }
    return descriptors.toArray(new ProblemDescriptor[descriptors.size()]);
  }

  static Pair<? extends PsiElement, ? extends PsiElement> getInteriorRange(PsiCodeBlock codeBlock) {
    PsiElement[] children = codeBlock.getChildren();
    if (children.length == 0) return Pair.create(codeBlock, codeBlock);
    int start;
    for (start=0; start<children.length;start++) {
      PsiElement child = children[start];
      if (child instanceof PsiWhiteSpace) continue;
      if (child instanceof PsiJavaToken && ((PsiJavaToken)child).getTokenType() == JavaTokenType.LBRACE) continue;
      break;
    }
    int end;
    for (end=children.length-1; end > start;end--) {
      PsiElement child = children[end];
      if (child instanceof PsiWhiteSpace) continue;
      if (child instanceof PsiJavaToken && ((PsiJavaToken)child).getTokenType() == JavaTokenType.RBRACE) continue;
      break;
    }
    return Pair.create(children[start], children[end]);
  }

  @Override
  @Nullable
  public ProblemDescriptor[] checkClass(@NotNull PsiClass aClass, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (!CHECK_TRY_CATCH_SECTION) return null;
    CatchBodyVisitor visitor = new CatchBodyVisitor(manager, new ArrayList<ProblemDescriptor>(), isOnTheFly);
    PsiClassInitializer[] initializers = aClass.getInitializers();
    for (PsiClassInitializer initializer : initializers) {
      initializer.accept(visitor);
    }

    return visitor.myProblemDescriptors.toArray(new ProblemDescriptor[visitor.myProblemDescriptors.size()]);
  }

  @Override
  @Nullable
  public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (!CHECK_FILE_HEADER) return null;
    ProblemDescriptor descriptor = FileHeaderChecker.checkFileHeader(file, manager, isOnTheFly);
    return descriptor == null ? null : new ProblemDescriptor[]{descriptor};
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Override
  @Nullable
  public JComponent createOptionsPanel() {
    return new InspectionOptions(this).getComponent();
  }

  public static LocalQuickFix createEditFileTemplateFix(final FileTemplate templateToEdit, final ReplaceWithFileTemplateFix replaceTemplateFix) {
    return new MyLocalQuickFix(templateToEdit, replaceTemplateFix);
  }

  private static class MyLocalQuickFix implements LocalQuickFix {
    private final FileTemplate myTemplateToEdit;
    private final ReplaceWithFileTemplateFix myReplaceTemplateFix;

    public MyLocalQuickFix(FileTemplate templateToEdit, ReplaceWithFileTemplateFix replaceTemplateFix) {
      myTemplateToEdit = templateToEdit;
      myReplaceTemplateFix = replaceTemplateFix;
    }

    @Override
    @NotNull
    public String getName() {
      return InspectionsBundle.message("default.file.template.edit.template");
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return getName();
    }

    @Override
    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      final FileTemplateConfigurable configurable = new FileTemplateConfigurable();
      SwingUtilities.invokeLater(new Runnable(){
        @Override
        public void run() {
          configurable.setTemplate(myTemplateToEdit, null);

          boolean ok = ShowSettingsUtil.getInstance().editConfigurable(project, configurable);
          if (ok) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              @Override
              public void run() {
                myReplaceTemplateFix.applyFix(project, descriptor);
              }
            });
          }
        }
      });
    }
  }
}
