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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.PayloadBuilder;

/**
 * This class is responsible to processing the data rows.
 * 
 * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
 * @since 1.0.1
 */
public class PushNotificationStep extends BaseStep implements StepInterface {

  /** for i18n purposes. **/
  private static final Class<?> PKG = PushNotificationStep.class;
  private PushNotificationStepMeta meta;
  private PushNotificationStepData data;

  public PushNotificationStep(
      StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
    super(s, stepDataInterface, c, t, dis);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    this.meta = (PushNotificationStepMeta) smi;
    this.data = (PushNotificationStepData) sdi;

    final Object[] r = getRow();
    if (r == null) {
      setOutputDone();
      return false;
    }
    try {
      if (first) {
        first = false;
        data.outputRowMeta = super.getInputRowMeta();
        data.nrPrevFields = data.outputRowMeta.size();
        meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

        data.insertRowMeta = new RowMeta();

        data.customFieldsDataPushValueNrs = new ArrayList<Integer>();
        data.locArgDataPushValueNrs = new ArrayList<Integer>();
        for (int i = 0; i < meta.getCustomFieldsDataPush().size(); i++) {
          data.customFieldsDataPushValueNrs.add(getInputRowMeta().indexOfValue(
              meta.getCustomFieldsDataPush().get(i)));
          if (data.customFieldsDataPushValueNrs.get(i) < 0) {
            throw new KettleStepException(BaseMessages.getString(PKG,
                "ApplePushNotification.Exception.FieldRequired", meta.getCustomFieldsDataPush()
                    .get(i)));
          }
        }
        for (int i = 0; i < meta.getLocalizedArgumentsDataPush().size(); i++) {
          data.locArgDataPushValueNrs.add(getInputRowMeta().indexOfValue(
              meta.getLocalizedArgumentsDataPush().get(i)));
          if (data.locArgDataPushValueNrs.get(i) < 0) {
            throw new KettleStepException(BaseMessages.getString(PKG,
                "ApplePushNotification.Exception.FieldRequired", meta
                    .getLocalizedArgumentsDataPush()
                    .get(i)));
          }
        }

        // Cache the position of the device token field
        cachePosition();

      } // end if first

      final Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
      for (int i = 0; i < data.nrPrevFields; i++) {
        outputRow[i] = r[i];
      }
      final Date res = sendPush(getInputRowMeta(), r);
      outputRow[data.nrPrevFields] = res;

      putRow(data.outputRowMeta, outputRow);

      if (checkFeedback(getLinesRead())) {
        if (log.isBasic()) {
          logBasic("Linenr " + getLinesRead()); // Some basic logging
        }

      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Sends the apple push notification.
   * 
   * @param rowMeta the row meta data.
   * @param r the row data.
   * @return the date of inactive date the registration id provided.
   * @throws KettleFileException the kettle file exception.
   * @throws KettleValueException the kettle value exception.
   */
  private Date sendPush(RowMetaInterface rowMeta, Object[] r) throws KettleFileException,
      KettleValueException {
    if (r == null) { // Stop: last line or error encountered
      if (log.isDetailed()) {
        logDetailed("Last line inserted: stop");
      }
      return null;
    }

    final InputStream fileInputStream =
        KettleVFS.getInputStream(environmentSubstitute(data.certificatePath));
    ApnsServiceBuilder apnsServiceBuilder =
        APNS.newService().withCert(fileInputStream, data.certificatePassword)
            .asBatched(data.waitTime, data.maxWaitTime)
            .withAppleDestination(!meta.isUseSandboxField());
    if (meta.isNoErrorDetectionField()) {
      apnsServiceBuilder = apnsServiceBuilder.withNoErrorDetection();
    }
    if (meta.isAsQueuedField()) {
      apnsServiceBuilder = apnsServiceBuilder.asQueued();
    }

    final ApnsService service = apnsServiceBuilder.build();
    PayloadBuilder payloadBuilder = APNS.newPayload();
    if (meta.getBadgeField() != null && !"".equals(meta.getBadgeField())) {
      payloadBuilder.badge(rowMeta.getInteger(r, data.indexOfBadgeField).intValue());
    }
    if (meta.isShrinksBodyField()) {
      payloadBuilder =
          payloadBuilder.shrinkBody(environmentSubstitute(meta.getShrinksPostfixField()));
    }
    if (meta.getSoundField() != null) {
      payloadBuilder = payloadBuilder.sound(rowMeta.getString(r, data.indexOfSoundField));
    }
    if (meta.getAlertBodyField() != null) {
      payloadBuilder = payloadBuilder.alertBody(rowMeta.getString(r, data.indexOfAlertBodyField));
    }
    if (meta.getActionLocalizedKeyField() != null) {
      payloadBuilder =
          payloadBuilder.actionKey(rowMeta.getString(r, data.indexOfActionLocalizedKeyField));
    }
    if (meta.getLocalizedKeyField() != null) {
      payloadBuilder =
          payloadBuilder.localizedKey(rowMeta.getString(r, data.indexOfLocalizedKeyField));
    }
    if (meta.getLocalizedArgumentsDataPush().size() > 0) {
      for (int i = 0; i < meta.getLocalizedArgumentsDataPush().size(); i++) {
        payloadBuilder = payloadBuilder.localizedArguments(getInputRowMeta().getString(r,
            data.locArgDataPushValueNrs.get(i)));
      }
    }
    if (meta.getLaunchImageField() != null) {
      payloadBuilder =
          payloadBuilder.launchImage(rowMeta.getString(r, data.indexOfLaunchImageField));
    }
    int idx = 0;
    for (String cFieldStream : meta.getCustomFieldsStream()) {
      payloadBuilder = payloadBuilder.customField(cFieldStream,
          getInputRowMeta().getString(r, data.customFieldsDataPushValueNrs.get(idx)));
      idx++;
    }
    final String payload = payloadBuilder.build();

    if (log.isDetailed()) {
      logDetailed("Payload: " + payload);
    }

    service.push(rowMeta.getString(r, data.indexOfDeviceTokenField), payload);

    final Map<String, Date> inactiveDevices = service.getInactiveDevices();
    return inactiveDevices.get(meta.getDeviceTokenField());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
    meta = (PushNotificationStepMeta) smi;
    data = (PushNotificationStepData) sdi;
    if (super.init(smi, sdi)) {
      if (meta.getCertificatePathField() != null) {
        data.certificatePath = environmentSubstitute(meta.getCertificatePathField());
      }
      if (meta.getCertificatePasswordField() != null) {
        data.certificatePassword = environmentSubstitute(meta.getCertificatePasswordField());
      }
      if (meta.getShrinksPostfixField() != null) {
        data.shrinksPostfix = environmentSubstitute(meta.getShrinksPostfixField());
      }
      if (meta.getWaitTimeField() != null) {
        data.waitTime = Integer.parseInt(environmentSubstitute(meta.getWaitTimeField()));
      }
      if (meta.getMaxWaitTimeField() != null) {
        data.maxWaitTime = Integer.parseInt(environmentSubstitute(meta.getMaxWaitTimeField()));
      }
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    super.dispose(smi, sdi);
  }

  /**
   * Run is were the action happens.
   */
  public void run() {
    logBasic("Starting to run...");
    try {
      while (processRow(meta, data) && !isStopped()) {
        continue;
      }
    } catch (Exception e) {
      logError("Unexpected error : " + e.toString());
      logError(Const.getStackTracker(e));
      setErrors(1);
      stopAll();
    } finally {
      dispose(meta, data);
      logBasic("Finished, processing " + getLinesRead() + " rows");
      markStop();
    }
  }

  /**
   * Checks the fields positions.
   * 
   * @throws KettleStepException the kettle step exception.
   */
  private void cachePosition() throws KettleStepException {
    if (meta.getDeviceTokenField() != null && data.indexOfDeviceTokenField < 0) {
      final String realDeviceToken = environmentSubstitute(meta.getDeviceTokenField());
      data.indexOfDeviceTokenField = getInputRowMeta().indexOfValue(realDeviceToken);
      if (data.indexOfDeviceTokenField < 0) {
        final String message =
            "Unable to find table name field [" + realDeviceToken + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getBadgeField() != null && data.indexOfBadgeField < 0) {
      final String realBadge = environmentSubstitute(meta.getBadgeField());
      data.indexOfBadgeField = getInputRowMeta().indexOfValue(realBadge);
      if (data.indexOfBadgeField < 0) {
        final String message = "Unable to find table name field [" + realBadge + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getSoundField() != null && data.indexOfSoundField < 0) {
      final String realSound = environmentSubstitute(meta.getSoundField());
      data.indexOfSoundField = getInputRowMeta().indexOfValue(realSound);
      if (data.indexOfSoundField < 0) {
        final String message = "Unable to find table name field [" + realSound + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getAlertBodyField() != null && data.indexOfAlertBodyField < 0) {
      final String realAlertBody = environmentSubstitute(meta.getAlertBodyField());
      data.indexOfAlertBodyField = getInputRowMeta().indexOfValue(realAlertBody);
      if (data.indexOfAlertBodyField < 0) {
        final String message =
            "Unable to find table name field [" + realAlertBody + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getActionLocalizedKeyField() != null && data.indexOfActionLocalizedKeyField < 0) {
      final String realActionLocalizedKey =
          environmentSubstitute(meta.getActionLocalizedKeyField());
      data.indexOfActionLocalizedKeyField = getInputRowMeta().indexOfValue(realActionLocalizedKey);
      if (data.indexOfActionLocalizedKeyField < 0) {
        final String message =
            "Unable to find table name field [" + realActionLocalizedKey + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getLocalizedKeyField() != null && data.indexOfLocalizedKeyField < 0) {
      final String realLocalizedKey = environmentSubstitute(meta.getLocalizedKeyField());
      data.indexOfLocalizedKeyField = getInputRowMeta().indexOfValue(realLocalizedKey);
      if (data.indexOfLocalizedKeyField < 0) {
        final String message =
            "Unable to find table name field [" + realLocalizedKey + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }

    if (meta.getLaunchImageField() != null && data.indexOfLaunchImageField < 0) {
      final String realLaunchImage = environmentSubstitute(meta.getLaunchImageField());
      data.indexOfLaunchImageField = getInputRowMeta().indexOfValue(realLaunchImage);
      if (data.indexOfLaunchImageField < 0) {
        final String message =
            "Unable to find table name field [" + realLaunchImage + "] in input row";
        logError(message);
        throw new KettleStepException(message);
      }
    }
  }
}
