/*
 * MIT License
 *
 * Copyright (c) 2018 Michael Buron, Olivier Grégoire, Rémi St-André
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package arreat.app.service;

import arreat.api.cfg.Configuration;
import arreat.api.message.Message;
import arreat.api.message.RegistryQueryMessage;
import arreat.api.registry.Registry;
import com.google.common.eventbus.Subscribe;
import java.util.Collections;
import java.util.Set;

/**
 * The registry service is use to interact with the registry of the users, remotes and rooms.
 */
public class RegistryService extends AbstractService {

  private static final String REGISTRY_IMPL_PROP = "registryImpl";

  private Registry registry;

  @Override
  public void configure(Configuration cfg) {
    if (!cfg.exists(REGISTRY_IMPL_PROP)) {
      throw new RuntimeException("Unable to configure registry service, missing registry implementation.");
    }

    try {
      this.registry = (Registry) this.createAndConfigure(cfg.getString(REGISTRY_IMPL_PROP), cfg);

    } catch (ClassCastException err) {
      throw new RuntimeException("Failed to configure registry service, expected registry implementation.", err);
    }
  }

  @Override
  public boolean asynchronous() {
    return false;
  }

  @Override
  public Set<Class<? extends Message>> getConsumedMessagesType() {
    return Collections.singleton(RegistryQueryMessage.class);
  }

  @Subscribe
  public void handleRegistryQueryMessage(RegistryQueryMessage query) {
    // TODO: Implement.
  }

  @Override
  public void run() {
    // No async job.
  }

  @Override
  public void terminate() {
    try {
      this.registry.close();

    } catch (Exception ok) {
      // Assume to registry was properly close, should not throw any exceptions.
    }
  }
}
