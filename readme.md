# Metaphone算法
    
    Metaphone语音匹配算法是Soundex的重大改进算法之一，首先发表在1990年的《Computer Language》杂志上。Metaphone语音匹配算法依据英语的发音规则进行编码处理，而这正是Soundex没有解决的问题。Metaphone在概念上和经典的Soundex相似,但是在语音编码的处理方法上要比Soundex丰富得多。例如，Metaphone算法包含一个明确的规则：在字母b在单词末尾出现在字母m后面时，就删除它。这个规则保证了lam和lamb会有相同的编码（LM），这样就使拼写检查应用程序能够为lam提供正确的替换。Metaphone算法整体上是一套规则集，可以把字母组合映射成辅音类。这个算法的Java实现需要几百行代码，具体可以参阅Apache Jakarta Commons Codec项目中的Metaphone代码。
    虽然在规则里仍然有一些缺陷，但Metaphone算法在Soundex上有了提高。例如Metaphone的作者Phillips指出，Bryan（BRYN）和Brian）BRN）应当有相同的代码。Phillips在2000年6月出版的C/C++ Users Journal上发表了他对Metaphone的模糊匹配（是这么叫的）改进的尝试。DoubleMetaphone算法对原来的辅音类做了一些修正,它把所有的开始元音都编码成A，所以不再使用Soundex算法。更加根本的变化是，DoubleMetaphone被编写成可以为多音词返回不同的代码。例如，hegemony中的g可以发轻声，也可以发重音，所以算法既返回HJMN，也可以返回HKMN。除了这些例子之外，Metaphone算法中的多数单词还是返回单一键。
    
    
    
# annotate
    
    展示了Jazzy中DoubleMeta.java的代码注释和个人理解，能力有限，难免错误和疏漏，欢迎交流指正。
