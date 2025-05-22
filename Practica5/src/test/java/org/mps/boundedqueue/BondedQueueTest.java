/* Realizado por Ignacio Morillas Rosell */
package org.mps.boundedqueue;

import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;

/*
 * TESTS:
 * 
 * - Constructor:
 *  - Constructor con capacidad negativa
 *  - Constructor con capacidad cero
 *  - Constructor con capacidad positiva
 *  
 * - Put:
 *  - Put en cola llena
 *  - Put con valor nulo
 *  - Put con valor válido
 *  - Put con varios valores válidos
 *  - Put en última posición del array
 * 
 * - Get:
 *  - Get en cola vacía
 *  - Get con valor válido
 *  - Get en última posición del array
 * 
 * - isFull:
 *  - isFull en cola llena
 *  - isFull en cola no llena
 * 
 * - isEmpty:
 *  - isEmpty en cola vacía
 *  - isEmpty en cola no vacía
 * 
 * - Size:
 *  - Size en cola vacía
 *  - Size en cola no vacía
 *  - Size en cola con elementos que no empiezan desde el índice 0
 * 
 * - getFirst:
 *  - getFirst en cola vacía
 *  - getFirst en cola no vacía
 *  - getFirst en cola con elementos que no empiezan desde el índice 0
 * 
 * - getLast:
 *  - getLast en cola vacía
 *  - getLast en cola no vacía
 *  - getLast en cola con elementos que no empiezan desde el índice 0
 *  - getLast en cola llena
 * 
 * - Iteración:
 *  - Iteración en cola vacía
 *  - Iteración en cola no vacía
 *  - Iteración en cola con elementos eliminados
 *  - Iteración en cola con elementos que no empiezan desde el índice 0
 *  - Iteración sobre el limite de la cola
 */

public class BondedQueueTest {
    private BoundedQueue<Integer> queue;
    private static final int CAPACITY = 5;

    @BeforeEach
    public void setUp() {
        queue = new ArrayBoundedQueue<>(CAPACITY);
    }

    @Nested
    @DisplayName("El constructor")
    class Constructor {
        @Test
        @DisplayName("lanza una excepción si la capacidad es negativa")
        void BondedQueue_constructorCapacidadNegativa_lanzaExcepcion() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new ArrayBoundedQueue<>(-1))
                .withMessage("ArrayBoundedException: capacity must be positive");
        }

        @Test
        @DisplayName("lanza una excepción si la capacidad es cero")
        void BondedQueue_constructorCapacidadNula_lanzaExcepcion() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new ArrayBoundedQueue<>(0))
                .withMessage("ArrayBoundedException: capacity must be positive");
        }

        @Test
        @DisplayName("crea la cola correctamente si la capacidad es positiva")
        void BondedQueue_constructorCapacidadPositiva_creaCola() {
            assertThatNoException()
                .isThrownBy(() -> {
                    queue = new ArrayBoundedQueue<>(1);
                });
            
            assertThat(queue)
                .isNotNull()
                .isInstanceOf(BoundedQueue.class);
        }
    }

    @Nested
    @DisplayName("Al insertar")
    class Put {
        @Test
        @DisplayName("lanza una excepción si la cola está llena")
        void put_colaLlena_lanzaExcepcion() {
            BoundedQueue<Integer> spyQueue = Mockito.spy(queue);
            
            // Simula que la cola está llena
            Mockito.doReturn(true).when(spyQueue).isFull();

            assertThatExceptionOfType(FullBoundedQueueException.class)
                .isThrownBy(() -> spyQueue.put(5))
                .withMessage("put: full bounded queue");
        }

        @Test
        @DisplayName("lanza una excepción si el valor es nulo")
        void put_valorNulo_lanzaExcepcion() {
            assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> queue.put(null))
                .withMessage("put: element cannot be null");
        }

        @Test
        @DisplayName("inserta el valor correctamente si la cola no está llena")
        void put_valorValido_insertaValor() {
            
            queue.put(5);

            assertThat(queue.size()).isEqualTo(1);
            assertThat(queue.getFirst()).isEqualTo(0);
            assertThat(queue.getLast()).isEqualTo(1);   // GetLast() devuelve el índice del siguiente elemento libre???
        }

        @Test
        @DisplayName("inserta varios valores correctamente")
        void put_variosValores_insertaValores() {
            queue.put(1);
            queue.put(2);
            queue.put(3);
            queue.put(4);
            queue.put(5);

            assertThat(queue.size()).isEqualTo(5);
            assertThat(queue.getFirst()).isEqualTo(0);
            assertThat(queue.getLast()).isEqualTo(0); // El último índice libre es 0 porque la cola está llena

            // Verifica que se llena toda la cola y no deja meter mas elementos
            assertThatExceptionOfType(FullBoundedQueueException.class)
                .isThrownBy(() -> queue.put(6))
                .withMessage("put: full bounded queue");
        }

        /* Uso reflexion para evitar añadir valores manualmente */
        @Test
        @DisplayName("si se encuentra en la ultima posición del array, lo mete en la primera posición")
        void put_valorEnUltimaPosicion_vuelveAlInicio() throws Exception {
            Field nextFree = queue.getClass().getDeclaredField("nextFree");
            Field array = queue.getClass().getDeclaredField("buffer");
            nextFree.setAccessible(true);
            array.setAccessible(true);
            nextFree.set(queue, 4); // Se establece el índice del siguiente elemento libre a 4 para que se inserte en la última posición del array
            Object[] buffer = (Object[]) array.get(queue); // Obtenemos el array del objeto queue para poder comprobar su contenido

            queue.put(0); // Se inserta el valor 0 en la última posición del array (índice 4)
            queue.put(1); // Se inserta el valor 1 en la primera posición del array (índice 0)

            // Verifica que el 1 se ha insertado en la primera posición del array (índice 0)
            assertThat(buffer[0]).isEqualTo(1);
            assertThat(buffer[4]).isEqualTo(0); // Verifica que el 0 se ha insertado en la última posición del array (índice 4)
            assertThat(queue.size()).isEqualTo(2); // La cola tiene 2 elementos
        }
    }

    @Nested
    @DisplayName("Al obtener")
    class Get {
        @Test
        @DisplayName("lanza una excepción si la cola está vacía")
        void get_colaVacia_lanzaExcepcion() {
            assertThatExceptionOfType(EmptyBoundedQueueException.class)
                .isThrownBy(() -> queue.get())
                .withMessage("get: empty bounded queue");
        }

        @Test
        @DisplayName("devuelve el primer elemento y lo elimina de la cola")
        void get_valorValido_devuelveYEliminaValor() {
            queue.put(1);
            queue.put(2);
            queue.put(3);

            Integer value = queue.get();

            assertThat(value).isEqualTo(1);
            assertThat(queue).doesNotContain(1); // El valor 1 ya no está en la cola
            assertThat(queue.size()).isEqualTo(2); // La cola ahora tiene 2 elementos
            assertThat(queue.getFirst()).isEqualTo(1); // El primer índice ahora es 1
            assertThat(queue.getLast()).isEqualTo(3); // El último índice libre es 3 porque hay 2 elementos en la cola y empieza en 1
        }

        /* Uso reflexion para evitar añadir valores manualmente */
        @Test
        @DisplayName("si se encuentra en la ultima posición del array, vuelve a empezar desde 0 tras sacar el valor")
        void get_valorValido_devuelveYEliminaValorVuelveAEmpezar() throws Exception {
            Field first = queue.getClass().getDeclaredField("first");
            Field nextFree = queue.getClass().getDeclaredField("nextFree");
            first.setAccessible(true);
            nextFree.setAccessible(true);
            first.set(queue, 4); // Se establece el índice del primer elemento a 4 (última posición del array)
            nextFree.set(queue, 4); // Se establece el índice del siguiente elemento libre a 4 para que se inserte en la última posición del array



            queue.put(0); // Se inserta el valor 0 en la última posición del array (índice 4)
            queue.put(1); // Se inserta el valor 1 en la primera posición del array (índice 0)
            queue.get();

            // Verifica que el primer índice ahora es 0
            assertThat(queue.getFirst()).isEqualTo(0);
            assertThat(queue).doesNotContain(0); // El valor 0 ya no está en la cola
            assertThat(queue.size()).isEqualTo(1); // La cola ahora tiene 1 elemento
        }
    }

    @Nested
    @DisplayName("Al comprobar si está llena o vacía")
    class IsFullOrEmpty {
        @Test
        @DisplayName("isFull devuelve true si la cola está llena")
        void isFull_colaLlena_devuelveTrue() {
            for (int i = 0; i < CAPACITY; i++) {
                queue.put(i);
            }

            assertThat(queue.isFull()).isTrue();
        }

        @Test
        @DisplayName("isFull devuelve false si la cola no está llena")
        void isFull_colaNoLlena_devuelveFalse() {
            assertThat(queue.isFull()).isFalse();
        }

        @Test
        @DisplayName("isEmpty devuelve true si la cola está vacía")
        void isEmpty_colaVacia_devuelveTrue() {
            assertThat(queue.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("isEmpty devuelve false si la cola no está vacía")
        void isEmpty_colaNoVacia_devuelveFalse() {
            queue.put(1);

            assertThat(queue.isEmpty()).isFalse();
        }
    }

    @Nested
    @DisplayName("Al comprobar el tamaño")
    class Size {
        @Test
        @DisplayName("devuelve 0 si la cola está vacía")
        void size_colaVacia_devuelveCero() {
            assertThat(queue.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("devuelve el tamaño correcto si la cola no está vacía")
        void size_colaNoVacia_devuelveTamanoCorrecto() {
            queue.put(1);
            queue.put(2);

            assertThat(queue.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("devuelve el tamaño correcto si los elementos no empiezan desde el indice 0")
        void size_elementosNoEmpiezanDesdeIndiceCero_devuelveTamanoCorrecto() throws Exception {
            queue.put(0);
            queue.get();
            queue.put(1);
            queue.put(2);
            queue.put(3);

            assertThat(queue.size()).isEqualTo(3); // La cola tiene 3 elementos
            assertThat(queue.getFirst()).isEqualTo(1); // El primer índice ahora es 1
        }
    }

    @Nested
    @DisplayName("Al obtener el primer índice")
    class GetFirst {
        @Test
        @DisplayName("devuelve 0 si la cola está vacía")
        void getFirst_colaVacia_devuelveCero() {
            assertThat(queue.getFirst()).isEqualTo(0); // El primer índice es 0
        }

        @Test
        @DisplayName("devuelve el índice del primer elemento si hay elementos")
        void getFirst_devuelveIndicePrimerElemento() {
            queue.put(1);
            queue.put(2);

            assertThat(queue.getFirst()).isEqualTo(0); // El primer índice es 0
        }

        @Test
        @DisplayName("devuelve el índice del nuevo primer elemento después de eliminar uno")
        void getFirst_valorEliminado_devuelveIndiceCorrecto() {
            queue.put(1);
            queue.put(2);
            queue.get(); // Elimina el primer elemento (1)

            assertThat(queue.getFirst()).isEqualTo(1); // El primer índice ahora es 1
        }
    }

    @Nested
    @DisplayName("Al obtener el último índice")
    class GetLast {
        @Test
        @DisplayName("devuelve 0 si la cola está vacía")
        void getLast_colaVacia_devuelveCero() {
            assertThat(queue.getLast()).isEqualTo(0); // El último índice libre es 0
        }

        @Test
        @DisplayName("devuelve el índice del siguiente elemento libre si hay elementos")
        void getLast_devuelveIndiceSiguienteElementoLibre() {
            queue.put(1);
            queue.put(2);

            assertThat(queue.getLast()).isEqualTo(2); // El último índice libre es 2
        }

        @Test
        @DisplayName("devuelve el índice del siguiente elemento libre después de eliminar uno")
        void getLast_valorEliminado_devuelveIndiceCorrecto() {
            queue.put(1);
            queue.put(2);
            queue.get(); // Elimina el primer elemento (1)

            assertThat(queue.getLast()).isEqualTo(2); // El último índice libre sigue siendo 2
        }

        @Test
        @DisplayName("devuelve el índice del primer elemento después de llenar la cola")
        void getLast_colaLlena_devuelveIndiceCorrecto() {
            for (int i = 0; i < CAPACITY; i++) {
                queue.put(i);
            }

            assertThat(queue.getLast()).isEqualTo(queue.getFirst()); // El último índice libre es el mismo que el primer índice porque la cola está llena
        }
    }

    @Nested
    @DisplayName("Al iterar sobre la cola")
    class Iteration {
        @Test
        @DisplayName("no hace nada si la cola está vacía")
        @SuppressWarnings("unused")
        void iterator_colaVacia_noHaceNada() {
            int count = 0;

            for (Integer value : queue) {
                count++;
            }

            assertThat(queue.isEmpty()).isTrue(); // La cola está vacía
            assertThat(count).isEqualTo(0); // No se han iterado elementos
        }

        @Test
        @DisplayName("itera correctamente sobre los elementos de la cola")
        void iterator_colaLlena_iterarElementos() {
            queue.put(1);
            queue.put(2);
            queue.put(3);

            int count = 0;
            for (Integer value : queue) {
                assertThat(value).isEqualTo(count + 1); // Los valores deben ser 1, 2, 3
                count++;
            }

            assertThat(count).isEqualTo(queue.size()); // Se han iterado todos los elementos
        }

        @Test
        @DisplayName("itera correctamente sobre los elementos de la cola después de eliminar uno")
        void iterator_valorEliminado_iterarElementos() {
            queue.put(1);
            queue.put(2);
            queue.put(3);
            queue.get(); // Elimina el primer elemento (1)

            int count = 0;
            for (Integer value : queue) {
                assertThat(value).isEqualTo(count + 2); // Los valores deben ser 2, 3
                count++;
            }

            assertThat(count).isEqualTo(queue.size()); // Se han iterado todos los elementos
        }

        @Test
        @DisplayName("itera correctamente de forma circular")
        void iterator_circular_iterarElementos() {
            queue.put(1);
            queue.get(); // Elimina el primer elemento (1)
            queue.put(2);
            queue.get();
            queue.put(3);
            queue.put(4);
            queue.put(5);
            queue.put(6); // Añade otro nuevo elemento para la cirularidad

            int count = 0;
            for (Integer value : queue) {
                assertThat(value).isEqualTo(count + 3); // Los valores deben ser 3, 4, 5, 6
                count++;
            }

            assertThat(count).isEqualTo(queue.size()); // Se han iterado todos los elementos
        }

        @Test
        @DisplayName("al intentar iterar más allá del tamaño de la cola, lanza excepción")
        void iterator_excedeTamano_lanzaExcepcion() {
            assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> {
                    queue.iterator().next(); // Intenta iterar más allá del tamaño de la cola
                })
                .withMessage("next: bounded queue iterator exhausted");
        }
    }

}
