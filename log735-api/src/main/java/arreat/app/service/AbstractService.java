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

import arreat.api.cfg.Configurable;
import arreat.api.cfg.Configuration;
import arreat.api.pubsub.ServiceBus;
import arreat.api.service.Service;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractService implements Service {

  protected ServiceBus bus;

  /**
   * Set the service bus for the publisher to publish.
   *
   * @param bus The service bus for publishing.
   */
  @Override
  public void setServiceBus(ServiceBus bus) {
    this.bus = bus;
  }

  /**
   * Create and configure all the component of the Net Service.
   *
   * @param className The name of the class of the component.
   * @param cfg The configuration to configure the configurable component.
   * @return The configured configurable.
   */
  protected Configurable createAndConfigure(String className, Configuration cfg) {
    Configurable configurable;

    try {
      Class<?> clz = Class.forName(className);

      // Make sure the class is an actual implementation of the configurable interface.
      if (Configurable.class.isAssignableFrom(clz)) {
        throw new RuntimeException("Fail to create net component, expected configurable.");
      }

      Constructor<?> ctor = clz.getConstructor();

      configurable = (Configurable) ctor.newInstance();

    } catch (ClassNotFoundException err) {
      throw new RuntimeException(
          "Fail to create net component, could not find implementing class.");

    } catch (NoSuchMethodException | IllegalAccessException err) {
      throw new RuntimeException(
          "Fail to instantiate net component, could not find default constructor.");

    } catch (InstantiationException | InvocationTargetException err) {
      throw new RuntimeException("Fail to instantiate net component, error during creation.", err);
    }

    configurable.configure(cfg);

    return configurable;
  }
}
