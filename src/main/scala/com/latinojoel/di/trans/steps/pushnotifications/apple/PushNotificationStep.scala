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

import org.pentaho.di.trans.step.BaseStep
import org.pentaho.di.trans.step.StepAttributesInterface
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.trans.TransMeta
import org.pentaho.di.trans.Trans
import org.pentaho.di.trans.step.StepMeta
import org.pentaho.di.trans.step.StepMetaInterface
import org.pentaho.di.trans.step.StepInterface
import org.pentaho.di.core.row.RowDataUtil
import org.pentaho.di.core.Const
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import org.pentaho.di.i18n.BaseMessages
import org.pentaho.di.core.exception.KettleStepException
import org.pentaho.di.core.row.RowMeta
import org.pentaho.di.core.row.ValueMetaInterface
import org.pentaho.di.core.row.RowMetaInterface
import org.pentaho.di.core.exception.KettleException
import org.pentaho.di.core.row.ValueMeta
import org.pentaho.di.core.RowMetaAndData
import com.notnoop.apns.APNS
import com.notnoop.apns.ApnsServiceBuilder
import com.notnoop.apns.ApnsService
import com.notnoop.apns.PayloadBuilder
import java.util.Date
import org.pentaho.di.core.vfs.KettleVFS

/**
 * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
 */
class PushNotificationStep(s: StepMeta, stepDataInterface: StepDataInterface, c: Int, t: TransMeta, dis: Trans) extends BaseStep(s: StepMeta, stepDataInterface: StepDataInterface, c: Int, t: TransMeta, dis: Trans) with StepInterface {
  val PKG: Class[PushNotificationStepDialog] = classOf[PushNotificationStepDialog]
  var meta: PushNotificationStepMeta = _
  var data: PushNotificationStepData = _

  override def processRow(smi: StepMetaInterface, sdi: StepDataInterface): Boolean = {
    this.meta = smi.asInstanceOf[PushNotificationStepMeta]
    this.data = sdi.asInstanceOf[PushNotificationStepData]

    var r: Array[Object] = getRow()
    if (r == null) {
      setOutputDone()
      return false
    }

    if (first) {
      first = false
      data.outputRowMeta = super.getInputRowMeta()
      data.NrPrevFields = data.outputRowMeta.size()
      meta.getFields(data.outputRowMeta, getStepname(), null, null, this)

      data.insertRowMeta = new RowMeta()

      data.customFieldsDataPushValueNrs = new ListBuffer[Int]
      data.locArgDataPushValueNrs = new ListBuffer[Int]
      var i: Int = 0
      while (i < meta.getCustomFieldsDataPush.length) {
        data.customFieldsDataPushValueNrs += getInputRowMeta().indexOfValue(meta.getCustomFieldsDataPush.apply(i))
        if (data.customFieldsDataPushValueNrs.apply(i) < 0) {
          throw new KettleStepException(BaseMessages.getString(PKG, "ApplePushNotification.Exception.FieldRequired", meta.getCustomFieldsDataPush.apply(i)))
        }
        i += 1
      }

      i = 0
      while (i < meta.getLocalizedArgumentsDataPush.length) {
        data.locArgDataPushValueNrs += getInputRowMeta().indexOfValue(meta.getLocalizedArgumentsDataPush.apply(i))
        if (data.locArgDataPushValueNrs.apply(i) < 0) {
          throw new KettleStepException(BaseMessages.getString(PKG, "ApplePushNotification.Exception.FieldRequired", meta.getLocalizedArgumentsDataPush.apply(i)))
        }
        i += 1
      }

      // Cache the position of the device token field
      cachePosition()

    } // end if first

    var outputRow: Array[Object] = RowDataUtil.allocateRowData(data.outputRowMeta.size())
    var i: Int = 0
    while (i < data.NrPrevFields) {
      outputRow(i) = r.apply(i)
      i += 1
    }
    val res: Date = sendPush(getInputRowMeta(), r)
    outputRow(data.NrPrevFields) = res

    putRow(data.outputRowMeta, outputRow)

    if (checkFeedback(getLinesRead())) {
      if (log.isBasic) logBasic("Linenr " + getLinesRead()) // Some basic logging
    }
    return true
  }

  private def sendPush(rowMeta: RowMetaInterface, r: Array[Object]): Date = {
    if (r == null) // Stop: last line or error encountered 
    {
      if (log.isDetailed()) logDetailed("Last line inserted: stop")
      return null
    }

    val fileInputStream = KettleVFS.getInputStream(environmentSubstitute(data.certificatePath))
    var apnsServiceBuilder: ApnsServiceBuilder = APNS.newService().withCert(fileInputStream, data.certificatePassword)
      .asBatched(data.waitTime, data.maxWaitTime).withAppleDestination(!meta.isUseSandboxField)
    if (meta.isNoErrorDetectionField)
      apnsServiceBuilder = apnsServiceBuilder.withNoErrorDetection()
    if (meta.isAsQueuedField)
      apnsServiceBuilder = apnsServiceBuilder.asQueued()

    val service: ApnsService = apnsServiceBuilder.build()
    var payloadBuilder: PayloadBuilder = APNS.newPayload()
    if (meta.getBadgeField != null && meta.getBadgeField.ne(""))
      payloadBuilder.badge(rowMeta.getInteger(r, data.indexOfBadgeField).intValue())
    if (meta.isShrinksBodyField)
      payloadBuilder = payloadBuilder.shrinkBody(environmentSubstitute(meta.getShrinksPostfixField))
    if (meta.getSoundField ne null)
      payloadBuilder = payloadBuilder.sound(rowMeta.getString(r, data.indexOfSoundField))
    if (meta.getAlertBodyField ne null)
      payloadBuilder = payloadBuilder.alertBody(rowMeta.getString(r, data.indexOfAlertBodyField))
    if (meta.getActionLocalizedKeyField ne null)
      payloadBuilder = payloadBuilder.actionKey(rowMeta.getString(r, data.indexOfActionLocalizedKeyField))
    if (meta.getLocalizedKeyField ne null)
      payloadBuilder = payloadBuilder.localizedKey(rowMeta.getString(r, data.indexOfLocalizedKeyField))
    if (meta.getLocalizedArgumentsDataPush.size > 0) {
      var i: Int = 0
      while (i < meta.getLocalizedArgumentsDataPush.size) {
        payloadBuilder = payloadBuilder.localizedArguments(getInputRowMeta().getString(r, data.locArgDataPushValueNrs.apply(i)))
        i = i + 1
      }
    }
    if (meta.getLaunchImageField ne null)
      payloadBuilder = payloadBuilder.launchImage(rowMeta.getString(r, data.indexOfLaunchImageField))
    var idx: Int = 0
    for (cFieldStream <- meta.getCustomFieldsStream) {
      payloadBuilder = payloadBuilder.customField(cFieldStream, getInputRowMeta().getString(r, data.customFieldsDataPushValueNrs.apply(idx)))
      idx = idx + 1
    }
    val payload: String = payloadBuilder.build()

    if (log.isDetailed()) logDetailed("Payload: " + payload)

    service.push(rowMeta.getString(r, data.indexOfDeviceTokenField), payload)

    val inactiveDevices: java.util.Map[String, Date] = service.getInactiveDevices()
    return inactiveDevices.get(meta.getDeviceTokenField)
  }

  override def init(smi: StepMetaInterface, sdi: StepDataInterface): Boolean = {
    meta = smi.asInstanceOf[PushNotificationStepMeta]
    data = sdi.asInstanceOf[PushNotificationStepData]
    if (super.init(smi, sdi)) {
      try {
        if (meta.getCertificatePathField ne null)
          data.certificatePath = environmentSubstitute(meta.getCertificatePathField)
        if (meta.getCertificatePasswordField ne null)
          data.certificatePassword = environmentSubstitute(meta.getCertificatePasswordField)
        if (meta.getShrinksPostfixField ne null)
          data.shrinksPostfix = environmentSubstitute(meta.getShrinksPostfixField)
        if (meta.getWaitTimeField != null)
          data.waitTime = Integer.parseInt(environmentSubstitute(meta.getWaitTimeField))
        if (meta.getMaxWaitTimeField != null)
          data.maxWaitTime = Integer.parseInt(environmentSubstitute(meta.getMaxWaitTimeField))
        return true
      } catch {
        case e: KettleException => {
          logError("An error occurred intialising this step: " + e.getMessage())
          stopAll()
          setErrors(1)
        }
      }
    }
    return false
  }

  override def dispose(smi: StepMetaInterface, sdi: StepDataInterface) = super.dispose(smi, sdi)

  // Run is were the action happens!
  def run() = {
    logBasic("Starting to run...")
    try {
      while (processRow(meta, data) && !isStopped()) {}
    } catch {
      case e: Exception => {
        logError("Unexpected error : " + e.toString())
        logError(Const.getStackTracker(e))
        setErrors(1)
        stopAll()
      }
    } finally {
      dispose(meta, data)
      logBasic("Finished, processing " + getLinesRead() + " rows")
      markStop()
    }
  }

  def cachePosition() = {
    if (meta.getDeviceTokenField != null && data.indexOfDeviceTokenField < 0) {
      val realDeviceToken: String = environmentSubstitute(meta.getDeviceTokenField)
      data.indexOfDeviceTokenField = getInputRowMeta().indexOfValue(realDeviceToken);
      if (data.indexOfDeviceTokenField < 0) {
        val message: String = "Unable to find table name field [" + realDeviceToken + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getBadgeField != null && data.indexOfBadgeField < 0) {
      val realBadge: String = environmentSubstitute(meta.getBadgeField)
      data.indexOfBadgeField = getInputRowMeta().indexOfValue(realBadge);
      if (data.indexOfBadgeField < 0) {
        val message: String = "Unable to find table name field [" + realBadge + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getSoundField != null && data.indexOfSoundField < 0) {
      val realSound: String = environmentSubstitute(meta.getSoundField)
      data.indexOfSoundField = getInputRowMeta().indexOfValue(realSound);
      if (data.indexOfSoundField < 0) {
        val message: String = "Unable to find table name field [" + realSound + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getAlertBodyField != null && data.indexOfAlertBodyField < 0) {
      val realAlertBody: String = environmentSubstitute(meta.getAlertBodyField)
      data.indexOfAlertBodyField = getInputRowMeta().indexOfValue(realAlertBody);
      if (data.indexOfAlertBodyField < 0) {
        val message: String = "Unable to find table name field [" + realAlertBody + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getActionLocalizedKeyField != null && data.indexOfActionLocalizedKeyField < 0) {
      val realActionLocalizedKey: String = environmentSubstitute(meta.getActionLocalizedKeyField)
      data.indexOfActionLocalizedKeyField = getInputRowMeta().indexOfValue(realActionLocalizedKey);
      if (data.indexOfActionLocalizedKeyField < 0) {
        val message: String = "Unable to find table name field [" + realActionLocalizedKey + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getLocalizedKeyField != null && data.indexOfLocalizedKeyField < 0) {
      val realLocalizedKey: String = environmentSubstitute(meta.getLocalizedKeyField)
      data.indexOfLocalizedKeyField = getInputRowMeta().indexOfValue(realLocalizedKey);
      if (data.indexOfLocalizedKeyField < 0) {
        val message: String = "Unable to find table name field [" + realLocalizedKey + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getLaunchImageField != null && data.indexOfLaunchImageField < 0) {
      val realLaunchImage: String = environmentSubstitute(meta.getLaunchImageField)
      data.indexOfLaunchImageField = getInputRowMeta().indexOfValue(realLaunchImage);
      if (data.indexOfLaunchImageField < 0) {
        val message: String = "Unable to find table name field [" + realLaunchImage + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }
  }
}