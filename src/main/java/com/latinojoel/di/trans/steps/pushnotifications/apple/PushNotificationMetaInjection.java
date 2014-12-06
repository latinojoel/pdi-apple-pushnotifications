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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.steps.denormaliser.DenormaliserTargetField;

/**
 * This class is responsible for the implementation of metadata injection.
 * 
 * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
 * @since 1.0.1
 */
public class PushNotificationMetaInjection implements StepMetaInjectionInterface {

  /** for i18n purposes. **/
  private static final Class<?> PKG = PushNotificationMetaInjection.class;

  private PushNotificationMeta meta;

  public PushNotificationMetaInjection(PushNotificationMeta pushNotificationMeta) {
    this.meta = pushNotificationMeta;
  }

  /**
   * Metadata injection entry fields.
   * 
   * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
   * @since 1.0.1
   */
  public enum EntryFields {

    DEVICE_TOKEN(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.DEVICE_TOKEN.Label")),
    BADGE(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.BADGE.Label")),
    SOUND(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.SOUND.Label")),
    ALERT_BODY(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.ALERT_BODY.Label")),
    ACTION_LOCALIZED_KEY(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.ACTION_LOCALIZED_KEY.Label")),
    LOCALIZED_KEY(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.LOCALIZED_KEY.Label")),
    LAUNCH_IMAGE(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.LAUNCH_IMAGE.Label")),
    CERTIFICATE_PATH(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.CERTIFICATE_PATH.Label")),
    CERTIFICATE_PASSWORD(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.CERTIFICATE_PASSWORD.Label")),
    RESPONSE(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.RESPONSE.Label")),
    SHRINKS_POSTFIX(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.SHRINKS_POSTFIX.Label")),
    WAIT_TIME(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.WAIT_TIME.Label")),
    MAX_WAIT_TIME(ValueMetaInterface.TYPE_STRING, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.MAX_WAIT_TIME.Label")),
    USE_SANDBOX(ValueMetaInterface.TYPE_BOOLEAN, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.USE_SANDBOX.Label")),
    SHRINKS_BODY(ValueMetaInterface.TYPE_BOOLEAN, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.SHRINKS_BODY.Label")),
    NO_ERROR_DETECTION(ValueMetaInterface.TYPE_BOOLEAN, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.NO_ERROR_DETECTION.Label")),
    AS_QUEUED(ValueMetaInterface.TYPE_BOOLEAN, BaseMessages.getString(PKG,
        "ApplePushNotificationMetaInj.AS_QUEUED.Label"));


    private int valueType;
    private String description;

    private EntryFields(int valueType, String description) {
      this.valueType = valueType;
      this.description = description;
    }

    public int getValueType() {
      return valueType;
    }

    public String getDescription() {
      return description;
    }

    public static EntryFields findEntry(String key) {
      return EntryFields.valueOf(key);
    }
  };

  /**
   * {@inheritDoc}
   */
  public List<StepInjectionMetaEntry> extractStepMetadataEntries() throws KettleException {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() throws KettleException {
    final List<StepInjectionMetaEntry> all = new ArrayList<StepInjectionMetaEntry>();

    final EntryFields[] topEntries = EntryFields.values();
    for (EntryFields topEntry : topEntries) {
      all.add(new StepInjectionMetaEntry(topEntry.name(), topEntry.getValueType(), topEntry
          .getDescription()));
    }

    return all;
  }

  /**
   * {@inheritDoc}
   */
  public void injectStepMetadataEntries(List<StepInjectionMetaEntry> all) throws KettleException {

    final List<DenormaliserTargetField> pushTargetFields =
        new ArrayList<DenormaliserTargetField>();

    for (StepInjectionMetaEntry lookFields : all) {
      final EntryFields fieldsEntry = EntryFields.findEntry(lookFields.getKey());
      if (fieldsEntry == null) {
        continue;
      }

      final String lookValue = (String) lookFields.getValue();
      switch (fieldsEntry) {
        case DEVICE_TOKEN:
          meta.setDeviceTokenField(lookValue);
          break;
        case BADGE:
          meta.setBadgeField(lookValue);
          break;
        case SOUND:
          meta.setSoundField(lookValue);
          break;
        case ALERT_BODY:
          meta.setAlertBodyField(lookValue);
          break;
        case ACTION_LOCALIZED_KEY:
          meta.setActionLocalizedKeyField(lookValue);
          break;
        case LOCALIZED_KEY:
          meta.setLocalizedKeyField(lookValue);
          break;
        case LAUNCH_IMAGE:
          meta.setLaunchImageField(lookValue);
          break;
        case CERTIFICATE_PATH:
          meta.setCertificatePathField(lookValue);
          break;
        case CERTIFICATE_PASSWORD:
          meta.setCertificatePasswordField(lookValue);
          break;
        case RESPONSE:
          meta.setResponseField(lookValue);
          ;
          break;
        case SHRINKS_POSTFIX:
          meta.setShrinksPostfixField(lookValue);
          break;
        case WAIT_TIME:
          meta.setWaitTimeField(lookValue);
          break;
        case MAX_WAIT_TIME:
          meta.setMaxWaitTimeField(lookValue);
          break;
        case USE_SANDBOX:
          meta.setUseSandboxField("Y".equalsIgnoreCase(lookValue));
          break;
        case SHRINKS_BODY:
          meta.setShrinksBodyField("Y".equalsIgnoreCase(lookValue));
          break;
        case NO_ERROR_DETECTION:
          meta.setNoErrorDetectionField("Y".equalsIgnoreCase(lookValue));
          break;
        case AS_QUEUED:
          meta.setAsQueuedField("Y".equalsIgnoreCase(lookValue));
          break;

      }
    }

  }

  public PushNotificationMeta getMeta() {
    return meta;
  }
}
