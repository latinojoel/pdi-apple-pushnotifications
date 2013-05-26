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

import org.pentaho.di.trans.step.BaseStepData
import org.pentaho.di.trans.step.StepDataInterface
import org.pentaho.di.core.row.RowMetaInterface
import scala.collection.mutable.ListBuffer

/**
 * @author <a href="mailto:jlatino@sapo.pt">Joel Latino</a>
 */
class PushNotificationStepData() extends BaseStepData() with StepDataInterface {

  var outputRowMeta: RowMetaInterface = _
  var insertRowMeta: RowMetaInterface = _
  var locArgDataPushValueNrs: ListBuffer[Int] = _
  var customFieldsDataPushValueNrs: ListBuffer[Int] = _
  var fieldnr: Int = _
  var NrPrevFields: Int = 0

  var certificatePath: String = _
  var certificatePassword: String = _
  var shrinksPostfix: String = _
  var waitTime: Int = 0
  var maxWaitTime: Int = 0

  var indexOfDeviceTokenField: Int = -1
  var indexOfBadgeField: Int = -1
  var indexOfSoundField: Int = -1
  var indexOfAlertBodyField: Int = -1
  var indexOfActionLocalizedKeyField: Int = -1
  var indexOfLocalizedKeyField: Int = -1
  var indexOfLaunchImageField: Int = -1
}