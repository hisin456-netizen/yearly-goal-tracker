package com.hoeseok.yearly_goal_tracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Java 환경에서 C의 malloc/free (네이티브 Off-Heap 메모리 할당/해제)를 테스트하는 예제
 */
public class MallocTest {

    public static void main(String[] args) throws Exception {
        MallocTest test = new MallocTest();
        System.out.println("==================================================");
        test.testDirectByteBufferMalloc();
        System.out.println("==================================================");
        test.testUnsafeMallocAndFree();
        System.out.println("==================================================");

        Field f = Unsafe.class.getDeclaredField("theUnsafe");
    }

    @Test
    @DisplayName("1. Java NIO DirectByteBuffer: 내부 JNI C malloc() 호출")
    public void testDirectByteBufferMalloc() {
        System.out.println("[테스트 1] Direct ByteBuffer (JVM 힙 외부 C malloc 호출)");

        int capacity = 20; // 20 bytes (int 5개 분량)
        // JVM Heap이 아닌 OS 네이티브 힙에 malloc()으로 직접 할당
        ByteBuffer buffer = ByteBuffer.allocateDirect(capacity);

        assertTrue(buffer.isDirect());
        System.out.println(" Direct 버퍼 여부: " + buffer.isDirect());
        System.out.println(" 할당 용량: " + buffer.capacity() + " bytes");

        // 값 쓰기
        for (int i = 1; i <= 5; i++) {
            buffer.putInt(i * 100);
        }

        // 읽기 모드로 전환
        buffer.flip();

        int idx = 0;
        while (buffer.hasRemaining()) {
            int val = buffer.getInt();
            System.out.println("  buffer[" + idx++ + "] = " + val);
        }
        System.out.println(" -> DirectByteBuffer 테스트 완료!\n");
    }

    @Test
    @DisplayName("2. sun.misc.Unsafe: C의 malloc/free 포인터 연산과 1:1 일치")
    public void testUnsafeMallocAndFree() throws Exception {
        System.out.println("[테스트 2] Unsafe.allocateMemory (C malloc/free와 1:1)");

        // Unsafe 인스턴스 획득 (리플렉션)
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);

        long bytes = 20; // 20 bytes
        // 1. C의 malloc(20) 호출 -> 네이티브 메모리 주소(long 포인터) 반환
        long address = unsafe.allocateMemory(bytes);

        System.out.println(" 할당된 64비트 네이티브 메모리 포인터: 0x" + Long.toHexString(address));

        try {
            // 2. 포인터 연산으로 메모리에 값 쓰기
            unsafe.putInt(address + 0, 1234);
            unsafe.putInt(address + 4, 5678);

            // 3. 값 읽기
            int val1 = unsafe.getInt(address + 0);
            int val2 = unsafe.getInt(address + 4);

            System.out.println("  *(address + 0) = " + val1);
            System.out.println("  *(address + 4) = " + val2);

            assertEquals(1234, val1);
            assertEquals(5678, val2);
        } finally {
            // 4. C의 free(address) 호출 -> 메모리 해제
            unsafe.freeMemory(address);
            System.out.println(" freeMemory(0x" + Long.toHexString(address) + ") 해제 완료!\n");
        }
    }
}
