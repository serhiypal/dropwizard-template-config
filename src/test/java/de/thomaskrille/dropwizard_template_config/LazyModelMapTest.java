package de.thomaskrille.dropwizard_template_config;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LazyModelMapTest {

    private static final String PROVIDER_1 = "provider_1";

    private static final String PROVIDER_2 = "provider_2";

    private static final String PROVIDER_1_KEY = "provider_1_key";

    private static final String PROVIDER_2_KEY = "provider_2_key";

    private static final String PROVIDER_1_VALUE = "provider_1_value";

    private static final String PROVIDER_2_VALUE = "provider_2_value";

    private static final Map<String, String> provider1Map = new HashMap<String, String>() {{ put(PROVIDER_1_KEY, PROVIDER_1_VALUE); }};

    private static final Map<String, String> provider2Map = new HashMap<String, String>() {{ put(PROVIDER_2_KEY, PROVIDER_2_VALUE); }};

    @Rule
    public MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    private TemplateConfigVariablesProvider provider1;

    @Mock
    private TemplateConfigVariablesProvider provider2;

    @Before
    public void setUp() {
        when(provider1.getNamespace()).thenReturn(PROVIDER_1);
        when(provider2.getNamespace()).thenReturn(PROVIDER_2);
        when(provider1.getVariables()).thenReturn(provider1Map);
        when(provider2.getVariables()).thenReturn(provider2Map);
    }

    @After
    public void tearDown() {
        Mockito.verifyNoMoreInteractions(provider1, provider2);
    }

    @Test
    public void containsKeyProvider1() {
        assertThat(new LazyModelMap(provider1, provider2).containsKey(PROVIDER_1)).isEqualTo(true);

        verify(provider1).getNamespace();
        verify(provider2).getNamespace();
    }

    @Test
    public void containsKeyProvider2() {
        assertThat(new LazyModelMap(provider1, provider2).containsKey(PROVIDER_2)).isEqualTo(true);

        verify(provider1).getNamespace();
        verify(provider2).getNamespace();
    }

    @Test
    public void getProvider1() {
        assertThat(new LazyModelMap(provider1, provider2).get(PROVIDER_1)).isSameAs(provider1Map);

        verify(provider1).getNamespace();
        verify(provider1).getVariables();
        verify(provider2).getNamespace();
    }

    @Test
    public void getProvider2() {
        assertThat(new LazyModelMap(provider1, provider2).get(PROVIDER_2)).isSameAs(provider2Map);

        verify(provider1).getNamespace();
        verify(provider2).getNamespace();
        verify(provider2).getVariables();
    }

    @Test
    public void isEmpty() {
        assertThat(new LazyModelMap().isEmpty()).isEqualTo(true);
    }

    @Test
    public void isNotEmpty() {
        assertThat(new LazyModelMap(provider1).isEmpty()).isEqualTo(false);

        verify(provider1).getNamespace();
    }

    @Test
    public void lazyLoadGet() {
        Map<String, Object> map = new LazyModelMap(provider1, provider2);
        assertThat(map.get(PROVIDER_2_KEY)).isEqualTo(PROVIDER_2_VALUE);
        assertThat(map.get(PROVIDER_2_KEY)).isEqualTo(PROVIDER_2_VALUE);
        assertThat(map.get(PROVIDER_1_KEY)).isEqualTo(PROVIDER_1_VALUE);
        assertThat(map.get(PROVIDER_1_KEY)).isEqualTo(PROVIDER_1_VALUE);

        verify(provider1, times(2)).getNamespace();
        verify(provider1, times(2)).getVariables();
        verify(provider2, times(2)).getNamespace();
        verify(provider2, times(2)).getVariables();
    }

    @Test
    public void put() {
        Map<String, Object> map = new LazyModelMap(provider1, provider2);
        map.put("test_key", "test_value");

        assertThat(map.get(PROVIDER_2_KEY)).isEqualTo(PROVIDER_2_VALUE);
        assertThat(map.get(PROVIDER_2_KEY)).isEqualTo(PROVIDER_2_VALUE);
        assertThat(map.get(PROVIDER_1_KEY)).isEqualTo(PROVIDER_1_VALUE);
        assertThat(map.get(PROVIDER_1_KEY)).isEqualTo(PROVIDER_1_VALUE);

        verify(provider1, times(2)).getNamespace();
        verify(provider1, times(2)).getVariables();
        verify(provider2, times(2)).getNamespace();
        verify(provider2, times(2)).getVariables();
    }

}
