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

import java.util.List;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * This class contains the methods to set and retrieve the status of the step data.
 * 
 * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
 * @since 1.0.1
 */
public class PushNotificationData extends BaseStepData implements StepDataInterface {
  RowMetaInterface outputRowMeta = null;
  RowMetaInterface insertRowMeta = null;
  List<Integer> locArgDataPushValueNrs = null;
  List<Integer> customFieldsDataPushValueNrs = null;
  int fieldnr = 0;
  int nrPrevFields = 0;

  String certificatePath = null;
  String certificatePassword = null;
  String shrinksPostfix = null;
  int waitTime = 0;
  int maxWaitTime = 0;

  int indexOfDeviceTokenField = -1;
  int indexOfBadgeField = -1;
  int indexOfSoundField = -1;
  int indexOfAlertBodyField = -1;
  int indexOfActionLocalizedKeyField = -1;
  int indexOfLocalizedKeyField = -1;
  int indexOfLaunchImageField = -1;
}
