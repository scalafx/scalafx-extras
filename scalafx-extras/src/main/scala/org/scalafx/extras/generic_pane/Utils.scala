/*
 * Copyright (c) 2011-2022, ScalaFX Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the ScalaFX Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SCALAFX PROJECT OR ITS CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.scalafx.extras.generic_pane

import java.awt.Desktop
import java.net.{URI, URL}

object Utils {


  /**
    * Opens URL using the default browser.
    *
    * @param url the URL to be displayed in the user default browser
    * @see Utils.openWebpage(uri: URI) for exceptions thrown
    */
  def openWebpage(url: URL): Unit = openWebpage(url.toURI)

  /**
    * Opens URI using the default browser.
    *
    * @param uri the URI to be displayed in the user default browser
    * @throws IllegalArgumentException      if uri is `null`
    * @throws HeadlessException             if GraphicsEnvironment.isHeadless() returns `true`
    * @throws UnsupportedOperationException if this Java Desktop class is not supported on the current platform or
    *                                       the current platform does not support the Desktop.Action.BROWSE action
    * @throws IOException                   if the user default browser is not found, or it fails to be launched,
    *                                       or the default handler application failed to be launched
    * @throws SecurityException             if a security manager exists and it denies the
    *                                       `AWTPermission("showWindowWithoutWarningBanner")` permission,
    *                                       or the calling thread is not allowed to create a subprocess
    */
  def openWebpage(uri: URI): Unit = {

    require(uri != null, "Argument 'uri' cannot be null.")

    if (Desktop.isDesktopSupported && Desktop.getDesktop.isSupported(Desktop.Action.BROWSE))
      Desktop.getDesktop.browse(uri)
  }
}
