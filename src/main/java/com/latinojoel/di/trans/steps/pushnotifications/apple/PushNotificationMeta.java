/**
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package com.latinojoel.di.trans.steps.pushnotifications.apple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * This class is responsible for implementing functionality regarding step meta. All Kettle steps
 * have an extension of this where private fields have been added with public accessors.
 * 
 * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
 * @since 1.0.1
 */
@Step(id = "ApplePushNotification", name = "ApplePushNotification.Step.Name",
    description = "ApplePushNotification.Step.Description",
    categoryDescription = "ApplePushNotification.Step.Category",
    image = "com/latinojoel/di/trans/steps/pushnotifications/apple/push.png",
    i18nPackageName = "com.latinojoel.di.trans.steps.pushnotifications.apple",
    casesUrl = "https://github.com/latinojoel", documentationUrl = "https://github.com/latinojoel",
    forumUrl = "https://github.com/latinojoel")
public class PushNotificationMeta extends BaseStepMeta implements StepMetaInterface {
  private static final Class<?> PKG = PushNotificationMeta.class; // for i18n purposes

  private String deviceTokenField, badgeField, soundField, alertBodyField, actionLocalizedKeyField,
      localizedKeyField, launchImageField, certificatePathField, certificatePasswordField,
      responseField,
      shrinksPostfixField, waitTimeField, maxWaitTimeField = null;
  private boolean useSandboxField, shrinksBodyField, noErrorDetectionField, asQueuedField = false;

  /**
   * Fields containing the values of custom fields in the input stream to send push notification.
   **/
  private List<String> customFieldsStream = null;

  /** Data of custom fields to send push notification. **/
  private List<String> customFieldsDataPush = null;

  /** Data of localized arguments to send push notification. **/
  private List<String> localizedArgumentsDataPush = null;

  public String getDeviceTokenField() {
    return deviceTokenField;
  }

  public void setDeviceTokenField(String deviceTokenField) {
    this.deviceTokenField = deviceTokenField;
  }

  public String getBadgeField() {
    return badgeField;
  }

  public void setBadgeField(String badgeField) {
    this.badgeField = badgeField;
  }

  public String getSoundField() {
    return soundField;
  }

  public void setSoundField(String soundField) {
    this.soundField = soundField;
  }

  public String getAlertBodyField() {
    return alertBodyField;
  }

  public void setAlertBodyField(String alertBodyField) {
    this.alertBodyField = alertBodyField;
  }

  public String getActionLocalizedKeyField() {
    return actionLocalizedKeyField;
  }

  public void setActionLocalizedKeyField(String actionLocalizedKeyField) {
    this.actionLocalizedKeyField = actionLocalizedKeyField;
  }

  public String getLocalizedKeyField() {
    return localizedKeyField;
  }

  public void setLocalizedKeyField(String localizedKeyField) {
    this.localizedKeyField = localizedKeyField;
  }

  public String getLaunchImageField() {
    return launchImageField;
  }

  public void setLaunchImageField(String launchImageField) {
    this.launchImageField = launchImageField;
  }

  public String getCertificatePathField() {
    return certificatePathField;
  }

  public void setCertificatePathField(String certificatePathField) {
    this.certificatePathField = certificatePathField;
  }

  public String getCertificatePasswordField() {
    return certificatePasswordField;
  }

  public void setCertificatePasswordField(String certificatePasswordField) {
    this.certificatePasswordField = certificatePasswordField;
  }

  public String getResponseField() {
    return responseField;
  }

  public void setResponseField(String responseField) {
    this.responseField = responseField;
  }

  public String getShrinksPostfixField() {
    return shrinksPostfixField;
  }

  public void setShrinksPostfixField(String shrinksPostfixField) {
    this.shrinksPostfixField = shrinksPostfixField;
  }

  public String getWaitTimeField() {
    return waitTimeField;
  }

  public void setWaitTimeField(String waitTimeField) {
    this.waitTimeField = waitTimeField;
  }

  public String getMaxWaitTimeField() {
    return maxWaitTimeField;
  }

  public void setMaxWaitTimeField(String maxWaitTimeField) {
    this.maxWaitTimeField = maxWaitTimeField;
  }

  public boolean isUseSandboxField() {
    return useSandboxField;
  }

  public void setUseSandboxField(boolean useSandboxField) {
    this.useSandboxField = useSandboxField;
  }

  public boolean isShrinksBodyField() {
    return shrinksBodyField;
  }

  public void setShrinksBodyField(boolean shrinksBodyField) {
    this.shrinksBodyField = shrinksBodyField;
  }

  public boolean isNoErrorDetectionField() {
    return noErrorDetectionField;
  }

  public void setNoErrorDetectionField(boolean noErrorDetectionField) {
    this.noErrorDetectionField = noErrorDetectionField;
  }

  public boolean isAsQueuedField() {
    return asQueuedField;
  }

  public void setAsQueuedField(boolean asQueuedField) {
    this.asQueuedField = asQueuedField;
  }

  public List<String> getCustomFieldsStream() {
    return customFieldsStream;
  }

  public void setCustomFieldsStream(List<String> customFieldsStream) {
    this.customFieldsStream = customFieldsStream;
  }

  public List<String> getCustomFieldsDataPush() {
    return customFieldsDataPush;
  }

  public void setCustomFieldsDataPush(List<String> customFieldsDataPush) {
    this.customFieldsDataPush = customFieldsDataPush;
  }

  public List<String> getLocalizedArgumentsDataPush() {
    return localizedArgumentsDataPush;
  }

  public void setLocalizedArgumentsDataPush(List<String> localizedArgumentsDataPush) {
    this.localizedArgumentsDataPush = localizedArgumentsDataPush;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getXML() {
    final StringBuilder retval = new StringBuilder();
    retval.append("    " + XMLHandler.addTagValue("deviceToken", deviceTokenField));
    retval.append("    " + XMLHandler.addTagValue("badge", badgeField));
    retval.append("    " + XMLHandler.addTagValue("sound", soundField));
    retval.append("    " + XMLHandler.addTagValue("alertBody", alertBodyField));
    retval.append("    " + XMLHandler.addTagValue("actionLocalizedKey", actionLocalizedKeyField));
    retval.append("    " + XMLHandler.addTagValue("localizedKey", localizedKeyField));
    retval.append("    " + XMLHandler.addTagValue("launchImage", launchImageField));
    retval.append("    " + XMLHandler.addTagValue("certificatePath", certificatePathField));
    retval.append("    " + XMLHandler.addTagValue("certificatePassword",
        certificatePasswordField));
    retval.append("    " + XMLHandler.addTagValue("responseField", responseField));
    retval.append("    " + XMLHandler.addTagValue("shrinksPostfix", shrinksPostfixField));
    retval.append("    " + XMLHandler.addTagValue("waitTime", waitTimeField));
    retval.append("    " + XMLHandler.addTagValue("maxWaitTime", maxWaitTimeField));
    retval.append("    " + XMLHandler.addTagValue("useSandbox", useSandboxField));
    retval.append("    " + XMLHandler.addTagValue("shrinksBody", shrinksBodyField));
    retval.append("    " + XMLHandler.addTagValue("noErrorDetection", noErrorDetectionField));
    retval.append("    " + XMLHandler.addTagValue("asQueued", asQueuedField));

    retval.append("    <customFields>").append(Const.CR);
    for (int i = 0; i < customFieldsDataPush.size(); i++) {
      retval.append("        <field>").append(Const.CR);
      retval.append("          ").append(
          XMLHandler.addTagValue("customFieldsStream", customFieldsStream.get(i)));
      retval.append("          ").append(
          XMLHandler.addTagValue("customFieldsDataPush", customFieldsDataPush.get(i)));
      retval.append("        </field>").append(Const.CR);
    }
    retval.append("    </customFields>").append(Const.CR);

    retval.append("    <localizedArguments>").append(Const.CR);
    for (String localizedArgument : localizedArgumentsDataPush) {
      retval.append("        <field>").append(Const.CR);
      retval.append("          ").append(
          XMLHandler.addTagValue("localizedArgumentsDataPush", localizedArgument));
      retval.append("        </field>").append(Const.CR);
    }
    retval.append("    </localizedArguments>").append(Const.CR);

    return retval.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void readRep(Repository rep, ObjectId idStep, List<DatabaseMeta> databases,
      Map<String, Counter> counters)
      throws KettleException {
    try {
      deviceTokenField = rep.getStepAttributeString(idStep, "deviceToken");
      badgeField = rep.getStepAttributeString(idStep, "badge");
      soundField = rep.getStepAttributeString(idStep, "sound");
      alertBodyField = rep.getStepAttributeString(idStep, "alertBody");
      actionLocalizedKeyField = rep.getStepAttributeString(idStep, "actionLocalizedKey");
      localizedKeyField = rep.getStepAttributeString(idStep, "localizedKey");
      launchImageField = rep.getStepAttributeString(idStep, "launchImage");
      certificatePathField = rep.getStepAttributeString(idStep, "certificatePath");
      certificatePasswordField = rep.getStepAttributeString(idStep, "certificatePassword");
      responseField = rep.getStepAttributeString(idStep, "responseField");
      shrinksPostfixField = rep.getStepAttributeString(idStep, "shrinksPostfix");
      waitTimeField = rep.getStepAttributeString(idStep, "waitTime");
      maxWaitTimeField = rep.getStepAttributeString(idStep, "maxWaitTime");
      useSandboxField = rep.getStepAttributeBoolean(idStep, "useSandbox");
      shrinksBodyField = rep.getStepAttributeBoolean(idStep, "shrinksBody");
      noErrorDetectionField = rep.getStepAttributeBoolean(idStep, "noErrorDetection");
      asQueuedField = rep.getStepAttributeBoolean(idStep, "asQueued");

      final int nrDataCols = rep.countNrStepAttributes(idStep, "localizedArgumentsDataPush");
      final int nrCols = rep.countNrStepAttributes(idStep, "customFieldsDataPush");
      final int nrStreams = rep.countNrStepAttributes(idStep, "customFieldsStream");
      final int nrRows = nrCols < nrStreams ? nrStreams : nrCols;
      customFieldsStream = new ArrayList<String>();
      customFieldsDataPush = new ArrayList<String>();
      localizedArgumentsDataPush = new ArrayList<String>();
      for (int i = 0; i < nrRows; i++) {
        customFieldsStream.add(Const.NVL(
            rep.getStepAttributeString(idStep, i, "customFieldsStream"), ""));
        customFieldsDataPush.add(Const.NVL(
            rep.getStepAttributeString(idStep, i, "customFieldsDataPush"), ""));
      }

      for (int i = 0; i < nrDataCols; i++) {
        localizedArgumentsDataPush.add(Const.NVL(
            rep.getStepAttributeString(idStep, i, "localizedArgumentsDataPush"), ""));
      }
    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(PKG,
          "ApplePushNotification.Exception.UnexpectedErrorInReadingStepInfo"), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void saveRep(Repository rep, ObjectId idTransformation, ObjectId idStep)
      throws KettleException {
    try {
      rep.saveStepAttribute(idTransformation, idStep, "deviceToken", deviceTokenField);
      rep.saveStepAttribute(idTransformation, idStep, "badge", badgeField);
      rep.saveStepAttribute(idTransformation, idStep, "sound", soundField);
      rep.saveStepAttribute(idTransformation, idStep, "alertBody", alertBodyField);
      rep.saveStepAttribute(idTransformation, idStep, "actionLocalizedKey",
          actionLocalizedKeyField);
      rep.saveStepAttribute(idTransformation, idStep, "localizedKey", localizedKeyField);
      rep.saveStepAttribute(idTransformation, idStep, "launchImage", launchImageField);
      rep.saveStepAttribute(idTransformation, idStep, "certificatePath", certificatePathField);
      rep.saveStepAttribute(idTransformation, idStep, "certificatePassword",
          certificatePasswordField);
      rep.saveStepAttribute(idTransformation, idStep, "responseField", responseField);
      rep.saveStepAttribute(idTransformation, idStep, "shrinksPostfix", shrinksPostfixField);
      rep.saveStepAttribute(idTransformation, idStep, "waitTime", waitTimeField);
      rep.saveStepAttribute(idTransformation, idStep, "maxWaitTime", maxWaitTimeField);
      rep.saveStepAttribute(idTransformation, idStep, "useSandbox", useSandboxField);
      rep.saveStepAttribute(idTransformation, idStep, "shrinksBody", shrinksBodyField);
      rep.saveStepAttribute(idTransformation, idStep, "noErrorDetection", noErrorDetectionField);
      rep.saveStepAttribute(idTransformation, idStep, "asQueued", asQueuedField);

      final int nrRows =
          customFieldsStream.size() < customFieldsDataPush.size() ? customFieldsDataPush.size()
              : customFieldsStream.size();
      for (int i = 0; i < nrRows; i++) {
        rep.saveStepAttribute(idTransformation, idStep, i, "customFieldsStream",
            i < customFieldsStream.size() ? customFieldsStream.get(i) : "");
        rep.saveStepAttribute(idTransformation, idStep, i, "customFieldsDataPush",
            i < customFieldsDataPush.size() ? customFieldsDataPush.get(i) : "");
      }

      for (int i = 0; i < localizedArgumentsDataPush.size(); i++) {
        rep.saveStepAttribute(idTransformation, idStep, i, "localizedArgumentsDataPush",
            localizedArgumentsDataPush.get(i));
      }
    } catch (Exception e) {
      throw new KettleException(BaseMessages.getString(PKG,
          "TemplateStep.Exception.UnableToSaveStepInfoToRepository") + idStep, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info,
      StepMeta nextStep,
      VariableSpace space) {
    // Just add the response field...
    if (responseField != null) {
      final ValueMetaInterface key = new ValueMeta(space.environmentSubstitute(responseField),
          ValueMetaInterface.TYPE_DATE);
      key.setOrigin(origin);
      r.addValueMeta(key);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object clone() {
    return super.clone();
  }

  /**
   * {@inheritDoc}
   * 
   * @throws KettleXMLException the kettle exception.
   */
  @Override
  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleXMLException {
    readData(stepnode);
  }

  /**
   * Reads data from XML step node.
   * 
   * @param stepnode the XML step node.
   * @throws KettleXMLException the kettle XML exception.
   */
  public void readData(Node stepnode) throws KettleXMLException {
    try {
      deviceTokenField = XMLHandler.getTagValue(stepnode, "deviceToken");
      badgeField = XMLHandler.getTagValue(stepnode, "badge");
      soundField = XMLHandler.getTagValue(stepnode, "sound");
      alertBodyField = XMLHandler.getTagValue(stepnode, "alertBody");
      actionLocalizedKeyField = XMLHandler.getTagValue(stepnode, "actionLocalizedKey");
      localizedKeyField = XMLHandler.getTagValue(stepnode, "localizedKey");
      launchImageField = XMLHandler.getTagValue(stepnode, "launchImage");
      certificatePathField = XMLHandler.getTagValue(stepnode, "certificatePath");
      certificatePasswordField = XMLHandler.getTagValue(stepnode, "certificatePassword");
      responseField = XMLHandler.getTagValue(stepnode, "responseField");
      shrinksPostfixField = XMLHandler.getTagValue(stepnode, "shrinksPostfix");
      waitTimeField = XMLHandler.getTagValue(stepnode, "waitTime");
      maxWaitTimeField = XMLHandler.getTagValue(stepnode, "maxWaitTime");
      useSandboxField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "useSandbox"));
      shrinksBodyField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "shrinksBody"));
      noErrorDetectionField =
          "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "noErrorDetection"));
      asQueuedField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "asQueued"));

      // Custom fields
      final Node customFields = XMLHandler.getSubNode(stepnode, "customFields");
      final int nrCustomFieldsRows = XMLHandler.countNodes(customFields, "field");
      customFieldsStream = new ArrayList<String>();
      customFieldsDataPush = new ArrayList<String>();
      for (int i = 0; i < nrCustomFieldsRows; i++) {
        final Node knode = XMLHandler.getSubNodeByNr(customFields, "field", i);
        customFieldsStream.add(XMLHandler.getTagValue(knode, "customFieldsStream"));
        customFieldsDataPush.add(XMLHandler.getTagValue(knode, "customFieldsDataPush"));
      }

      // Localized Arguments
      final Node localizedArgumentsFields = XMLHandler.getSubNode(stepnode, "localizedArguments");
      final int nrlocalizedArgumentsRows = XMLHandler.countNodes(localizedArgumentsFields,
          "field");
      localizedArgumentsDataPush = new ArrayList<String>();
      for (int i = 0; i < nrlocalizedArgumentsRows; i++) {
        final Node knode = XMLHandler.getSubNodeByNr(localizedArgumentsFields, "field", i);
        localizedArgumentsDataPush.add(XMLHandler.getTagValue(knode,
            "localizedArgumentsDataPush"));
      }
    } catch (Exception e) {
      throw new KettleXMLException(BaseMessages.getString(PKG,
          "ApplePushNotification.Exception.UnexpectedErrorInReadingStepInfo"), e);
    }
  }

  /**
   * Sets the default values.
   */
  public void setDefault() {
    this.deviceTokenField = "";
    this.badgeField = "";
    this.soundField = "";
    this.alertBodyField = "";
    this.actionLocalizedKeyField = "";
    this.localizedKeyField = "";
    this.launchImageField = "";
    this.certificatePathField = "";
    this.certificatePasswordField = "";
    this.responseField = "";
    this.shrinksPostfixField = "";
    this.waitTimeField = "";
    this.maxWaitTimeField = "";
    this.useSandboxField = false;
    this.shrinksBodyField = false;
    this.noErrorDetectionField = false;
    this.asQueuedField = false;
    this.customFieldsStream = new ArrayList<String>();
    this.customFieldsDataPush = new ArrayList<String>();
    this.localizedArgumentsDataPush = new ArrayList<String>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta,
      RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
    CheckResult cr = null;
    if (prev == null || prev.size() == 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG,
          "ApplePushNotification.CheckResult.NotReceivingFields"), stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,
          "ApplePushNotification.CheckResult.StepRecevingData", prev.size() + ""), stepMeta);
      remarks.add(cr);

      if (deviceTokenField == null || "".equals(deviceTokenField) || "0".equals(deviceTokenField)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
            "ApplePushNotification.CheckResult.DeviceTokenDefined"), stepMeta);
        remarks.add(cr);
      }
      if (certificatePathField == null || "".equals(certificatePathField)
          || "0".equals(certificatePathField)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
            "ApplePushNotification.CheckResult.CertificatePathDefined"), stepMeta);
        remarks.add(cr);
      }
      if (certificatePasswordField == null || "".equals(certificatePasswordField)
          || "0".equals(certificatePasswordField)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
            "ApplePushNotification.CheckResult.CertificatePasswordDefined"), stepMeta);
        remarks.add(cr);
      }

      if (maxWaitTimeField == null || "".equals(maxWaitTimeField) || "0".equals(maxWaitTimeField)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
            "ApplePushNotification.CheckResult.MaxWaitTimeDefined"), stepMeta);
        remarks.add(cr);
      }
      if (waitTimeField == null || "".equals(waitTimeField) || "0".equals(waitTimeField)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
            "ApplePushNotification.CheckResult.WaitTimeDefined"), stepMeta);
        remarks.add(cr);
      }

      String errorMessage = "";
      boolean errorFound = false;
      // Starting from selected fields in ...
      for (String f : localizedArgumentsDataPush) {
        if (prev.indexOfValue(f) < 0) {
          errorMessage += "\t\t" + f + Const.CR;
          errorFound = true;
        }
      }
      if (errorFound) {
        errorMessage = BaseMessages.getString(PKG,
            "ApplePushNotification.CheckResult.LocalizedArgumentsFieldsNotFound", errorMessage);
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta);
        remarks.add(cr);
      } else {
        if (localizedArgumentsDataPush.size() > 0) {
          cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,
              "ApplePushNotification.CheckResult.LocalizedArguments.AllFieldsFound"), stepMeta);
          remarks.add(cr);
        } else {
          cr =
              new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG,
                  "ApplePushNotification.CheckResult.LocalizedArguments.NoFieldsEntered"),
                  stepMeta);
          remarks.add(cr);
        }
      }

      errorMessage = "";
      errorFound = false;
      // Starting from selected fields in ...
      for (String f : customFieldsDataPush) {
        if (prev.indexOfValue(f) < 0) {
          errorMessage += "\t\t" + f + Const.CR;
          errorFound = true;
        }
      }
      if (errorFound) {
        errorMessage = BaseMessages.getString(PKG,
            "ApplePushNotification.CheckResult.CustomFieldsFieldsNotFound", errorMessage);
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, errorMessage, stepMeta);
        remarks.add(cr);
      } else {
        if (customFieldsDataPush.size() > 0) {
          cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,
              "ApplePushNotification.CheckResult.CustomFields.AllFieldsFound"), stepMeta);
          remarks.add(cr);
        } else {
          cr =
              new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG,
                  "ApplePushNotification.CheckResult.CustomFields.NoFieldsEntered"), stepMeta);
          remarks.add(cr);
        }
      }

      // See if we have input streams leading to this step!
      if (input.length > 0) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG,
            "ApplePushNotification.CheckResult.StepRecevingData2"), stepMeta);
        remarks.add(cr);
      } else {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG,
            "ApplePushNotification.CheckResult.NoInputReceivedFromOtherSteps"), stepMeta);
        remarks.add(cr);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean supportsErrorHandling() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return new PushNotificationMetaInjection(this);
  }

  /**
   * Get the dialog interface step.
   * 
   * @param shell the shell.
   * @param meta the step info.
   * @param transMeta the transformation info.
   * @param name the name.
   * @return The appropriate StepDialogInterface class.
   */
  public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta,
      String name) {
    return new PushNotificationDialog(shell, (BaseStepMeta) meta, transMeta, name);
  }

  /**
   * Get the executing step, needed by Trans to launch a step.
   * 
   * @param stepMeta The step info.
   * @param stepDataInterface the step data interface linked to this step. Here the step can store
   *        temporary data, database connections, etc.
   * @param cnr The copy nr to get.
   * @param transMeta The transformation info.
   * @param disp The launching transformation.
   * @return The appropriate StepInterface class.
   */
  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
      TransMeta transMeta,
      Trans disp) {
    return new PushNotification(stepMeta, stepDataInterface, cnr, transMeta, disp);
  }

  /**
   * Get a new instance of the appropriate data class. This data class implements the
   * StepDataInterface. It basically contains the persisting data that needs to live on, even if a
   * worker thread is terminated.
   * 
   * @return The appropriate StepDataInterface class.
   */
  public StepDataInterface getStepData() {
    return new PushNotificationData();
  }

}
