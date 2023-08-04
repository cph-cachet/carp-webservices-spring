/**
 * Copyright 2018 Copenhagen Center for Health Technology (CACHET) at the Technical University of Denmark (DTU).
 *
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED ”AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package dk.cachet.carp.webservices.common.configuration.properties

import org.springframework.context.annotation.*
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

/**
 * The Configuration Class [PropertiesConfig].
 * The [PropertiesConfig] defines the properties that specify the sources of the environment properties.
 */
@Configuration
@ComponentScan(basePackages=["dk.cachet.carp.webservices"])
@PropertySources(PropertySource(value = ["classpath:config/application-\${spring.profiles.active}.yml"]))
class PropertiesConfig
{
    /**
     * The function [propertySourcesPlaceholderConfigurer] resolves the placeholders within bean definition property values
     * and annotations against the current Spring Environment and its set of [PropertySources].
     * @return The [PropertySourcesPlaceholderConfigurer].
     */
    @Bean
    fun propertySourcesPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer?
    {
        return PropertySourcesPlaceholderConfigurer()
    }
}
