😕 ## 参考网站

markdown语法:https://blog.csdn.net/CyrusCJA/article/details/156235995

dagger 文档:https://zhuanlan.zhihu.com/p/709431082

### Dagger 基本概念

###### 依赖需求方： 就是需要依赖对象的那些类。 例如一个人想要玩电脑，那么他就必须得有一台电脑，电脑是依赖对象，这个人依赖于这台电脑，因此这个人就是依赖需求方；

###### 依赖供应方：负责提供依赖对象，类似与实际编码中的工厂类。 这个人依赖一台电脑玩游戏，那么就必须有个地方能够提供一台电脑，他可以去实体店买，也可以去网上买，而这个能向这个人提供电脑的地方就是依赖供应方。顾名思义，就是创建依赖对象的地方；

###### 依赖注入器：负责将依赖对象注入到依赖需求方，在实际代码中是一个接口，编译时 Dagger2 会自动生成这个接口的实现类。 接着上面的说，这个人是依赖需求方，他需要一台电脑，依赖供应方能够提供一台电脑，可是这两者没有打通，电脑没有给到这个人，他还是玩不了游戏啊，因此这个时候就需要依赖注入器将这台电脑注入给这个人

### Dagger 导入

` dependencies { implementation 'com.google.dagger:dagger:2.57.2' annotationProcessor 'com.google.dagger:dagger-compiler:2.57.2' }`

1. 依赖需求方

```java
 //Person 类，里面有一个 playGame 的方法，这个方法中要使用 Computer，也就是说，
// Computer 是 Person 的依赖，因此我们使其成为一个成员变量：

public class Person {
    private String name;
    private Computer computer;

    public void Person(String name) {
        this.name = name;  
    }

    public void playGame(String gameName) {
        System.out.print(name + "\n\t");
        computer.play(gameName);
    }
}

//以下是 Computer 类，作为依赖对象，后面我们将通过 Dagger 设置到 Person 中的 computer 成员变量：
    public class Computer {
  
        private String name;
  
        public Computer(String name) {
            this.name = name;
        }
  
        public void play(String game) {
            System.out.println("使用 " + name + " 玩 " + game);
        }
    }
```

2. 依赖供应方

```java
//要找到依赖提供商，并从依赖供应方拿一台电脑。哪里能提供电脑呢？京东、淘宝、实体店都行，这里就先编写一个淘宝类吧
    @Module
    public class TaoBao {
  
        private Computer assembleComputer() {         //组装一台电脑
            Computer computer = new Computer("淘宝组装的电脑");
            return computer;
        }
  
        @Provides
        public Computer getComputer() {
            return assembleComputer();
        }
    }

```

3. 依赖注入器

```java
//有了需求方和供应方，那么就需要将两者连接起来，依赖对象只有从供应方交给需求方，才有意义。这就像厂商生产的商品只有卖给用户，才能发挥商品的作用，经济才能发展。
//那么连接这两者的这就是依赖注入器的工作。在这个例子中，依赖注入器就是快递了，快递把电脑从淘宝店家送到买家手中

//@Component 这个注解的 modules 属性是一个 Class<?>[] 数组，因此可以让依赖注入器指定不止一个依赖供应方。例如，这个例子中，中通不仅可以从淘宝拿电脑进行配送，
    @Component(modules = TaoBao.class)
    public interface ZTOExpress {
        void deliverTo(Person person);
    }
```

### Dagger 提供了两种方式创建依赖对象：
* 调用被 @Inject 注解标识的构造方法
* 调用被 @Module 注解的类中提供相应的 @Provides 方法

### Dagger 提供依赖流程可以概括为如下：
1. 查找 @Module 类中是否存在创建该类的方法
2. 如果存在，查看该方法是否存在参数
    1. 存在参数，则按从步骤1 开始依次初始化每个参数
    2. 不存在参数，则直接初始化该类实例，注入到依赖需求方
3. 如果不存在，则查找该类中被 @Inject 标识的构造方法
      1. 如果构造函数有参数，则按照从步骤1 开始依次初始化每个参数
      2. 如果构造函数没有参数，则直接初始化该类实例，一次依赖注入到此结束

**也就是说，Dagger 提供创建依赖对象的过程是递归的。**

### @Scope 范围注解时，一定要注意两点：
> 如果是通过依赖对象的构造函数创建依赖时，需要在类名上添加范围注解，不能在构造函数上添加，否则无效。 - 范围内单例的前提是使用了相同的依赖注入器。

## 注解

1. @Module 用于告知 Dagger 这个类是一个依赖提供商，这样 Dagger 才能够识别
2. @Provides 用于告知 Dagger 这个依赖提供商里面哪些方法是用于提供依赖对象的。当 Dagger 需要创建一个依赖对象时，它会查找被 @Module 标识的类中被 @Provides 标识的方法，并根据所需依赖对象的类型，
3. @Component(modules = {TaoBao.class, JD.class})依赖注入器是一个 interface 而非 class，在编译时，Dagger 会生成对应的实现类。 这个接口添加了一个注解：@Component，这个注解是就是告诉注入器，从哪个依赖供应方拿依赖对象。这段代码里，@Component 注解告知了中通，去淘宝这个供应商拿到电脑并快递给买家。
4. @Inject (1.用在成员变量）注解了。Person 类里有 name 和 computer 两个成员变量，从名字上就能看到电脑肯定要配送到 computer 的成员变量上，这个时候需要将 computer 这个成员变量添加 @Inject 注解
5. @Inject (2.用在构造函数）它的作用就是当依赖注入器在所拥有的依赖供应商处查找依赖的提供方法时，如果找不到对应类型的依赖提供方法，那么 Dagger 就会去找这个依赖对象的类型有没有用 @Inject 声明的构造方法，如果有，那就通过这个构造方法生成这个依赖对象
6. @Named 注解了，这个注解使 Dagger 可以在返回值类型一样的情况下，再继续判断 @Named 注解的 value 值
7. @Qualifier 这个单词在英语中就是限定器的意思，顾名思义，在 Dagger 里肯定就是在类型相同时再进一步做个限定。它是一个元注解，@Named 就是继承于它。那我们怎么用这个注解呢？答案就是像 @Named一样，自定义一个注解继承 @Qualifier。 现在我们使用 @Qulifier 实现与上面相同的功能
8. @Singleton 就是 Dagger 可以定义一个某某范围，在这个某某范围内，不会创建多个依赖对象，而是仅创建一个
9. @Scope @Singleton 注解的作用。在这里就表示，通过中通从淘宝上拿到的硬盘都是这一块。但是这样也不太对，中通肯定不止为张三配送，那它为李四配送的时候，岂不是也送的张三的硬盘？ 所以这时候就别用自带的 @Singleton 范围，而是自定义一个范围，也就是使用 @Scope 注解。现在我们就为张三创建一个专属的范围，通过这个例子咱们也会明白 @Scope 的使用
10. @SubComponent 定义子组件、即子依赖注入器 ,如果这个依赖注入器太复杂，那就应该划分为若干个子的依赖注入器，这就要用到 @SubComponent 这个注解了
11. x

