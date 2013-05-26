/*
 * Pentaho Data Integration Apple Push Notifications
 * https://github.com/latinojoel/pdi-apple-pushnotifications
 *
 * Copyright (c) 2009 about.me/latinojoel
 *
 * Licensed under the GNU General Public License, Version 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl-3.0.html
 *
 * The program is provided "as is" without any warranty express or implied, including
 * the warranty of non-infringement and the implied warranties of merchantibility and
 * fitness for a particular purpose.  The Copyright owner will not be liable for any
 * damages suffered by you as a result of using the Program.  In no event will the
 * Copyright owner be liable for any special, indirect or consequential damages or
 * lost profits even if the Copyright owner has been advised of the possibility of
 * their occurrence.
 */
package com.latinojoel.di.trans.steps.pushnotifications.apple

import org.pentaho.di.trans.step.StepMetaInterface
import org.pentaho.di.trans.step.BaseStepMeta
import org.pentaho.di.core.Const
import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.core.variables.VariableSpace
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.core.row.ValueMetaInterface
import org.pentaho.di.core.row.ValueMeta
import org.w3c.dom.Node
import org.pentaho.di.core.Counter
import org.pentaho.di.core.database.DatabaseMeta
import org.pentaho.di.core.xml.XMLHandler
import org.pentaho.di.core.exception.KettleXMLException
import org.pentaho.di.core.CheckResultInterface._
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.core.CheckResult
import org.pentaho.di.trans.step.StepDialogInterface
import org.eclipse.swt.widgets.Shell
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.repository.ObjectId
import org.pentaho.di.repository.Repository
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.i18n.BaseMessages
import java.util.Map
import scala.collection.mutable.ListBuffer
import java.util.List
import org.pentaho.di.core.CheckResultInterface

/**
 * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
 */
class PushNotificationStepMeta extends BaseStepMeta with StepMetaInterface {
  def PKG: Class[PushNotificationStepMeta] = classOf[PushNotificationStepMeta] // for i18n purposes
  private var deviceTokenField, badgeField, soundField, alertBodyField, actionLocalizedKeyField, localizedKeyField, launchImageField, certificatePathField, certificatePasswordField, responseField, shrinksPostfixField, waitTimeField, maxWaitTimeField: String = _
  private var useSandboxField, shrinksBodyField, noErrorDetectionField, asQueuedField: Boolean = false

  /** Fields containing the values of custom fields in the input stream to send push notification */
  private var customFieldsStream: ListBuffer[String] = _
  /** Data of custom fields to send push notification */
  private var customFieldsDataPush: ListBuffer[String] = _
  /** Data of localized arguments to send push notification */
  private var localizedArgumentsDataPush: ListBuffer[String] = _

  def getDeviceTokenField(): String = this.deviceTokenField
  def setDeviceTokenField(deviceTokenField: String) = this.deviceTokenField = deviceTokenField

  def getBadgeField(): String = this.badgeField
  def setBadgeField(badgeField: String) = this.badgeField = badgeField

  def getSoundField(): String = this.soundField
  def setSoundField(soundField: String) = this.soundField = soundField

  def getAlertBodyField(): String = this.alertBodyField
  def setAlertBodyField(alertBodyField: String) = this.alertBodyField = alertBodyField

  def getActionLocalizedKeyField(): String = this.actionLocalizedKeyField
  def setActionLocalizedKeyField(actionLocalizedKeyField: String) = this.actionLocalizedKeyField = actionLocalizedKeyField

  def getLocalizedKeyField(): String = this.localizedKeyField
  def setLocalizedKeyField(localizedKeyField: String) = this.localizedKeyField = localizedKeyField

  def getLaunchImageField(): String = this.launchImageField
  def setLaunchImageField(launchImageField: String) = this.launchImageField = launchImageField

  def getCertificatePathField(): String = this.certificatePathField
  def setCertificatePathField(certificatePathField: String) = this.certificatePathField = certificatePathField

  def getCertificatePasswordField(): String = this.certificatePasswordField
  def setCertificatePasswordField(certificatePasswordField: String) = this.certificatePasswordField = certificatePasswordField

  def getResponseField(): String = this.responseField
  def setResponseField(responseField: String) = this.responseField = responseField

  def getShrinksPostfixField(): String = this.shrinksPostfixField
  def setShrinksPostfixField(shrinksPostfixField: String) = this.shrinksPostfixField = shrinksPostfixField

  def getWaitTimeField(): String = this.waitTimeField
  def setWaitTimeField(waitTimeField: String) = this.waitTimeField = waitTimeField

  def getMaxWaitTimeField(): String = this.maxWaitTimeField
  def setMaxWaitTimeField(maxWaitTimeField: String) = this.maxWaitTimeField = maxWaitTimeField

  def isUseSandboxField(): Boolean = this.useSandboxField
  def setUseSandboxField(useSandboxField: Boolean) = this.useSandboxField = useSandboxField

  def isShrinksBodyField(): Boolean = this.shrinksBodyField
  def setShrinksBodyField(shrinksBodyField: Boolean) = this.shrinksBodyField = shrinksBodyField

  def isNoErrorDetectionField(): Boolean = this.noErrorDetectionField
  def setNoErrorDetectionField(noErrorDetectionField: Boolean) = this.noErrorDetectionField = noErrorDetectionField

  def isAsQueuedField(): Boolean = this.asQueuedField
  def setAsQueuedField(asQueuedField: Boolean) = this.asQueuedField = asQueuedField

  def getCustomFieldsStream(): ListBuffer[String] = this.customFieldsStream
  def setCustomFieldsStream(customFieldsStream: ListBuffer[String]) = this.customFieldsStream = customFieldsStream

  def getCustomFieldsDataPush(): ListBuffer[String] = this.customFieldsDataPush
  def setCustomFieldsDataPush(customFieldsDataPush: ListBuffer[String]) = this.customFieldsDataPush = customFieldsDataPush

  def getLocalizedArgumentsDataPush(): ListBuffer[String] = this.localizedArgumentsDataPush
  def setLocalizedArgumentsDataPush(localizedArgumentsDataPush: ListBuffer[String]) = this.localizedArgumentsDataPush = localizedArgumentsDataPush

  override def getXML(): String = {
    val retval: StringBuilder = new StringBuilder()
    retval.append("    " + XMLHandler.addTagValue("deviceToken", deviceTokenField))
    retval.append("    " + XMLHandler.addTagValue("badge", badgeField))
    retval.append("    " + XMLHandler.addTagValue("sound", soundField))
    retval.append("    " + XMLHandler.addTagValue("alertBody", alertBodyField))
    retval.append("    " + XMLHandler.addTagValue("actionLocalizedKey", actionLocalizedKeyField))
    retval.append("    " + XMLHandler.addTagValue("localizedKey", localizedKeyField))
    retval.append("    " + XMLHandler.addTagValue("launchImage", launchImageField))
    retval.append("    " + XMLHandler.addTagValue("certificatePath", certificatePathField))
    retval.append("    " + XMLHandler.addTagValue("certificatePassword", certificatePasswordField))
    retval.append("    " + XMLHandler.addTagValue("responseField", responseField))
    retval.append("    " + XMLHandler.addTagValue("shrinksPostfix", shrinksPostfixField))
    retval.append("    " + XMLHandler.addTagValue("waitTime", waitTimeField))
    retval.append("    " + XMLHandler.addTagValue("maxWaitTime", maxWaitTimeField))
    retval.append("    " + XMLHandler.addTagValue("useSandbox", useSandboxField))
    retval.append("    " + XMLHandler.addTagValue("shrinksBody", shrinksBodyField))
    retval.append("    " + XMLHandler.addTagValue("noErrorDetection", noErrorDetectionField))
    retval.append("    " + XMLHandler.addTagValue("asQueued", asQueuedField))

    retval.append("    <customFields>").append(Const.CR)
    var i: Int = 0
    while (i < customFieldsDataPush.length) {
      retval.append("        <field>").append(Const.CR)
      retval.append("          ").append(XMLHandler.addTagValue("customFieldsStream", customFieldsStream.apply(i)))
      retval.append("          ").append(XMLHandler.addTagValue("customFieldsDataPush", customFieldsDataPush.apply(i)))
      retval.append("        </field>").append(Const.CR)
      i += 1
    }
    retval.append("    </customFields>").append(Const.CR)

    retval.append("    <localizedArguments>").append(Const.CR)
    i = 0
    while (i < localizedArgumentsDataPush.length) {
      retval.append("        <field>").append(Const.CR)
      retval.append("          ").append(XMLHandler.addTagValue("localizedArgumentsDataPush", localizedArgumentsDataPush.apply(i)))
      retval.append("        </field>").append(Const.CR)
      i += 1
    }
    retval.append("    </localizedArguments>").append(Const.CR)

    retval.toString()
  }

  override def readRep(rep: Repository, id_step: ObjectId, databases: List[DatabaseMeta], counters: Map[String, Counter]) = {
    try {
      deviceTokenField = rep.getStepAttributeString(id_step, "deviceToken")
      badgeField = rep.getStepAttributeString(id_step, "badge")
      soundField = rep.getStepAttributeString(id_step, "sound")
      alertBodyField = rep.getStepAttributeString(id_step, "alertBody")
      actionLocalizedKeyField = rep.getStepAttributeString(id_step, "actionLocalizedKey")
      localizedKeyField = rep.getStepAttributeString(id_step, "localizedKey")
      launchImageField = rep.getStepAttributeString(id_step, "launchImage")
      certificatePathField = rep.getStepAttributeString(id_step, "certificatePath")
      certificatePasswordField = rep.getStepAttributeString(id_step, "certificatePassword")
      responseField = rep.getStepAttributeString(id_step, "responseField")
      shrinksPostfixField = rep.getStepAttributeString(id_step, "shrinksPostfix")
      waitTimeField = rep.getStepAttributeString(id_step, "waitTime")
      maxWaitTimeField = rep.getStepAttributeString(id_step, "maxWaitTime")
      useSandboxField = rep.getStepAttributeBoolean(id_step, "useSandbox")
      shrinksBodyField = rep.getStepAttributeBoolean(id_step, "shrinksBody")
      noErrorDetectionField = rep.getStepAttributeBoolean(id_step, "noErrorDetection")
      asQueuedField = rep.getStepAttributeBoolean(id_step, "asQueued")

      val nrDataCols: Int = rep.countNrStepAttributes(id_step, "localizedArgumentsDataPush")
      val nrCols: Int = rep.countNrStepAttributes(id_step, "customFieldsDataPush")
      val nrStreams: Int = rep.countNrStepAttributes(id_step, "customFieldsStream")
      val nrRows: Int = if (nrCols < nrStreams) nrStreams else nrCols
      customFieldsStream = new ListBuffer[String]
      customFieldsDataPush = new ListBuffer[String]
      localizedArgumentsDataPush = new ListBuffer[String]
      var i: Int = 0
      while (i < nrRows) {
        customFieldsStream += Const.NVL(rep.getStepAttributeString(id_step, i, "customFieldsStream"), "")
        customFieldsDataPush += Const.NVL(rep.getStepAttributeString(id_step, i, "customFieldsDataPush"), "")
        i += 1
      }

      i = 0
      while (i < nrDataCols) {
        localizedArgumentsDataPush += Const.NVL(rep.getStepAttributeString(id_step, i, "localizedArgumentsDataPush"), "")
        i += 1
      }
    } catch {
      case e: Exception => throw new KettleException(BaseMessages.getString(PKG, "ApplePushNotification.Exception.UnexpectedErrorInReadingStepInfo"), e)
    }
  }

  override def saveRep(rep: Repository, id_transformation: ObjectId, id_step: ObjectId) = {
    try {
      rep.saveStepAttribute(id_transformation, id_step, "deviceToken", deviceTokenField)
      rep.saveStepAttribute(id_transformation, id_step, "badge", badgeField)
      rep.saveStepAttribute(id_transformation, id_step, "sound", soundField)
      rep.saveStepAttribute(id_transformation, id_step, "alertBody", alertBodyField)
      rep.saveStepAttribute(id_transformation, id_step, "actionLocalizedKey", actionLocalizedKeyField)
      rep.saveStepAttribute(id_transformation, id_step, "localizedKey", localizedKeyField)
      rep.saveStepAttribute(id_transformation, id_step, "launchImage", launchImageField)
      rep.saveStepAttribute(id_transformation, id_step, "certificatePath", certificatePathField)
      rep.saveStepAttribute(id_transformation, id_step, "certificatePassword", certificatePasswordField)
      rep.saveStepAttribute(id_transformation, id_step, "responseField", responseField)
      rep.saveStepAttribute(id_transformation, id_step, "shrinksPostfix", shrinksPostfixField)
      rep.saveStepAttribute(id_transformation, id_step, "waitTime", waitTimeField)
      rep.saveStepAttribute(id_transformation, id_step, "maxWaitTime", maxWaitTimeField)
      rep.saveStepAttribute(id_transformation, id_step, "useSandbox", useSandboxField)
      rep.saveStepAttribute(id_transformation, id_step, "shrinksBody", shrinksBodyField)
      rep.saveStepAttribute(id_transformation, id_step, "noErrorDetection", noErrorDetectionField)
      rep.saveStepAttribute(id_transformation, id_step, "asQueued", asQueuedField)

      val nrRows: Int = if (customFieldsStream.length < customFieldsDataPush.length) customFieldsDataPush.length else customFieldsStream.length
      var i: Int = 0
      while (i < nrRows) {
        rep.saveStepAttribute(id_transformation, id_step, i, "customFieldsStream", if (i < customFieldsStream.length) customFieldsStream.apply(i) else "")
        rep.saveStepAttribute(id_transformation, id_step, i, "customFieldsDataPush", if (i < customFieldsDataPush.length) customFieldsDataPush.apply(i) else "")
        i += 1
      }

      i = 0
      while (i < localizedArgumentsDataPush.length) {
        rep.saveStepAttribute(id_transformation, id_step, i, "localizedArgumentsDataPush", localizedArgumentsDataPush.apply(i))
        i += 1
      }
    } catch {
      case e: Exception => throw new KettleException(BaseMessages.getString(PKG, "TemplateStep.Exception.UnableToSaveStepInfoToRepository") + id_step, e)
    }
  }

  override def getFields(r: RowMetaInterface, origin: String, info: Array[RowMetaInterface], nextStep: StepMeta, space: VariableSpace) = {
    // Just add the response field...
    if (responseField != null) {
      val key: ValueMetaInterface = new ValueMeta(space.environmentSubstitute(responseField), ValueMetaInterface.TYPE_DATE);
      key.setOrigin(origin);
      r.addValueMeta(key);
    }
  }

  override def clone(): Object = super.clone()

  override def loadXML(stepnode: Node, databases: List[DatabaseMeta], counters: Map[String, Counter]) = readData(stepnode)

  def readData(stepnode: Node) = {
    try {
      deviceTokenField = XMLHandler.getTagValue(stepnode, "deviceToken")
      badgeField = XMLHandler.getTagValue(stepnode, "badge")
      soundField = XMLHandler.getTagValue(stepnode, "sound")
      alertBodyField = XMLHandler.getTagValue(stepnode, "alertBody")
      actionLocalizedKeyField = XMLHandler.getTagValue(stepnode, "actionLocalizedKey")
      localizedKeyField = XMLHandler.getTagValue(stepnode, "localizedKey")
      launchImageField = XMLHandler.getTagValue(stepnode, "launchImage")
      certificatePathField = XMLHandler.getTagValue(stepnode, "certificatePath")
      certificatePasswordField = XMLHandler.getTagValue(stepnode, "certificatePassword")
      responseField = XMLHandler.getTagValue(stepnode, "responseField")
      shrinksPostfixField = XMLHandler.getTagValue(stepnode, "shrinksPostfix")
      waitTimeField = XMLHandler.getTagValue(stepnode, "waitTime")
      maxWaitTimeField = XMLHandler.getTagValue(stepnode, "maxWaitTime")
      useSandboxField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "useSandbox"))
      shrinksBodyField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "shrinksBody"))
      noErrorDetectionField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "noErrorDetection"))
      asQueuedField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "asQueued"))

      // Custom fields
      val customFields: Node = XMLHandler.getSubNode(stepnode, "customFields")
      val nrCustomFieldsRows: Int = XMLHandler.countNodes(customFields, "field")
      customFieldsStream = new ListBuffer[String]
      customFieldsDataPush = new ListBuffer[String]
      var i: Int = 0
      while (i < nrCustomFieldsRows) {
        val knode: Node = XMLHandler.getSubNodeByNr(customFields, "field", i)
        customFieldsStream += XMLHandler.getTagValue(knode, "customFieldsStream")
        customFieldsDataPush += XMLHandler.getTagValue(knode, "customFieldsDataPush")
        i += 1
      }

      // Localized Arguments
      val localizedArgumentsFields: Node = XMLHandler.getSubNode(stepnode, "localizedArguments")
      val nrlocalizedArgumentsRows: Int = XMLHandler.countNodes(localizedArgumentsFields, "field")
      localizedArgumentsDataPush = new ListBuffer[String]
      i = 0
      while (i < nrlocalizedArgumentsRows) {
        val knode: Node = XMLHandler.getSubNodeByNr(localizedArgumentsFields, "field", i)
        localizedArgumentsDataPush += XMLHandler.getTagValue(knode, "localizedArgumentsDataPush")
        i += 1
      }
    } catch {
      case e: Exception => throw new KettleException(BaseMessages.getString(PKG, "ApplePushNotification.Exception.UnexpectedErrorInReadingStepInfo"), e)
    }
  }

  def setDefault() = {
    this.deviceTokenField = ""
    this.badgeField = ""
    this.soundField = ""
    this.alertBodyField = ""
    this.actionLocalizedKeyField = ""
    this.localizedKeyField = ""
    this.launchImageField = ""
    this.certificatePathField = ""
    this.certificatePasswordField = ""
    this.responseField = ""
    this.shrinksPostfixField = ""
    this.waitTimeField = ""
    this.maxWaitTimeField = ""
    this.useSandboxField = false
    this.shrinksBodyField = false
    this.noErrorDetectionField = false
    this.asQueuedField = false
    this.customFieldsStream = new ListBuffer[String]
    this.customFieldsDataPush = new ListBuffer[String]
    this.localizedArgumentsDataPush = new ListBuffer[String]
  }

  override def check(remarks: List[CheckResultInterface], transmeta: TransMeta, stepMeta: StepMeta, prev: RowMetaInterface, input: Array[String], output: Array[String], info: RowMetaInterface): Unit = {
    var cr: CheckResult = null
    if (prev == null || prev.size() == 0) {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.NotReceivingFields"), stepMeta)
      remarks.add(cr)
    } else {
      cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.StepRecevingData", prev.size() + ""), stepMeta)
      remarks.add(cr)

      if (deviceTokenField == null || deviceTokenField.equals("") || deviceTokenField.equals(0)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.DeviceTokenDefined"), stepMeta)
        remarks.add(cr)
      }
      if (certificatePathField == null || certificatePathField.equals("") || certificatePathField.equals(0)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.CertificatePathDefined"), stepMeta)
        remarks.add(cr)
      }
      if (certificatePasswordField == null || certificatePasswordField.equals("") || certificatePasswordField.equals(0)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.CertificatePasswordDefined"), stepMeta)
        remarks.add(cr)
      }

      if (maxWaitTimeField == null || maxWaitTimeField.equals("") || maxWaitTimeField.equals(0)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.MaxWaitTimeDefined"), stepMeta)
        remarks.add(cr)
      }
      if (waitTimeField == null || waitTimeField.equals("") || waitTimeField.equals(0)) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.WaitTimeDefined"), stepMeta)
        remarks.add(cr)
      }

      var error_message: String = ""
      var error_found: Boolean = false;
      // Starting from selected fields in ...
      for (f <- localizedArgumentsDataPush; if prev.indexOfValue(f) < 0) {
        error_message += "\t\t" + f + Const.CR
        error_found = true
      }
      if (error_found) {
        error_message = BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.LocalizedArgumentsFieldsNotFound", error_message)
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta)
        remarks.add(cr)
      } else {
        if (localizedArgumentsDataPush.length > 0) {
          cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.LocalizedArguments.AllFieldsFound"), stepMeta)
          remarks.add(cr)
        } else {
          cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.LocalizedArguments.NoFieldsEntered"), stepMeta)
          remarks.add(cr)
        }
      }

      error_message = ""
      error_found = false;
      // Starting from selected fields in ...
      for (f <- customFieldsDataPush; if prev.indexOfValue(f) < 0) {
        error_message += "\t\t" + f + Const.CR
        error_found = true
      }
      if (error_found) {
        error_message = BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.CustomFieldsFieldsNotFound", error_message)
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta)
        remarks.add(cr)
      } else {
        if (customFieldsDataPush.length > 0) {
          cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.CustomFields.AllFieldsFound"), stepMeta)
          remarks.add(cr)
        } else {
          cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.CustomFields.NoFieldsEntered"), stepMeta)
          remarks.add(cr)
        }
      }

      // See if we have input streams leading to this step!
      if (input.length > 0) {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.StepRecevingData2"), stepMeta)
        remarks.add(cr)
      } else {
        cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ApplePushNotification.CheckResult.NoInputReceivedFromOtherSteps"), stepMeta)
        remarks.add(cr)
      }
    }
  }

  def getDialog(shell: Shell, meta: StepMetaInterface, transMeta: TransMeta, name: String): StepDialogInterface =
    new PushNotificationStepDialog(shell, meta.asInstanceOf[BaseStepMeta], transMeta, name)

  def getStep(stepMeta: StepMeta, stepDataInterface: StepDataInterface, cnr: Int, transMeta: TransMeta, disp: Trans): StepInterface =
    new PushNotificationStep(stepMeta, stepDataInterface, cnr, transMeta, disp)

  def getStepData(): StepDataInterface = new PushNotificationStepData()

}