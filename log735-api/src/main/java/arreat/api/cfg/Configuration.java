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

package arreat.api.cfg;


/**
 * Configuration object used by the Arreat API. This object is used to be serialized to YAML back
 * and forth.
 */
public interface Configuration {

  /**
   * Returns whether or not a property exists.
   *
   * @param name  The of the property to check.
   * @return  If it exists.
   */
  default boolean exists(String name) {
    return this.getProperty(name) != null;
  }

  /**
   * Returns the boolean value of a property. Will returns true only for the true value all other
   * case will return false.
   *
   * @param name  The name of the property to returns.
   * @return  Boolean value of the property.
   */
  default boolean getBoolean(String name) {
    return Boolean.parseBoolean(this.getString(name));
  }

  /**
   * Returns the integer value of a property. If the property isn't an Integer or doesn't exist it
   * returns null.
   *
   * @param name  The name of the property to returns.
   * @return  Integer value of the property.
   */
  default Integer getInteger(String name) {
    Integer integer = null;

    try {
      integer = Integer.valueOf(this.getString(name));

    } catch (NumberFormatException ok) {
      // Simply returns null.
    }
    return integer;
  }

  /**
   * Returns the value of a property. If the property doesn't exists null is returned.
   *
   * @param name  The name of the property to returns.
   * @return
   */
  Object getProperty(String name);

  default String getString(String name) {
    return this.exists(name) ? String.valueOf(this.getProperty(name)) : null;
  }

  /**
   * Sets the value of a specified property.
   *
   * @param name  The name of the property.
   * @param value The value of the property.
   */
  void setProperty(String name, Object value);
}