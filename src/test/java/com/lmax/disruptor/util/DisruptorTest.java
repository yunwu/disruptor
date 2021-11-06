package com.lmax.disruptor.util;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

public class DisruptorTest {


    @Test
    public void testDemo(String[] args){
        int size = 16;
        EventFactory<User> eventFactory = new EventFactory<User>() {
            private int age = 0;
            @Override
            public User newInstance() {
                age++;
                return new User("lili"+ age, age);
            }
        };

        EventHandler<User> eventHandler = new EventHandler<User>()
        {
            @Override
            public void onEvent(final User event, final long sequence, final boolean endOfBatch) throws Exception
            {
                System.out.println("消费事件 name:" + event.getName() + ", age:" + event.getAge());
            }
        };        Disruptor<User> userDisruptor = new Disruptor<User>(eventFactory, size, Executors.defaultThreadFactory(), ProducerType.SINGLE,new YieldingWaitStrategy());
        userDisruptor.handleEventsWith(eventHandler);
        userDisruptor.start();

        RingBuffer<User> userRingBuffer = userDisruptor.getRingBuffer();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i=0; i < 10000; i++){
                    //操作sequence cursor(游标)
                    long index = userRingBuffer.next();
                    User user = userRingBuffer.get(index);
                    user.setAge(18);
                    userRingBuffer.publish(index);
                }
            }
        }).start();
    }



    static class User{
        String name;
        int age;

        public User(String name, int age){
            this.name = name;
            this.age = age;
        }
        public String getName(){
            return this.name;
        }
        public int getAge(){
            return this.age;
        }
        public void setName(String name){
            this.name = name;
        }

        public void setAge(int age){
            this.age = age;
        }
    }
}
