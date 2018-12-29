/*
Jazzy - a Java library for Spell Checking
Copyright (C) 2001 Mindaugas Idzelis
Full text of license can be found in LICENSE.txt

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.swabunga.spell.engine;

/**
 * A phonetic encoding algorithm that takes an English word and computes a
 * phonetic version of it. This allows for phonetic matches in a spell checker.
 * This class is a port of the C++ DoubleMetaphone() class, which was intended
 * to return two possible phonetic translations for certain words, although the
 * Java version only seems to be concerned with one, making the "double" part
 * erroneous. <br>
 * source code for the original C++ can be found here: <a href=
 * "http://aspell.sourceforge.net/metaphone/"/>http://aspell.sourceforge.net/metaphone/</a>
 * DoubleMetaphone does some processing, such as uppercasing, on the input
 * string first to normalize it. Then, to create the key, the function traverses
 * the input string in a while loop, sending successive characters into a giant
 * switch statement. Before determining the appropriate pronunciation, the
 * algorithm considers the context surrounding each character within the input
 * string.
 * <p>
 * Things that were changed: <br/>
 * The alternate flag could be set to true but was never checked so why bother
 * with it. REMOVED <br/>
 * Why was this class serializable? <br/>
 * The primary, in, length and last variables could be initialized and local to
 * the process method and references passed around the appropriate methods. As
 * such there are no class variables and this class becomes firstly threadsafe
 * and secondly could be static final. <br/>
 * The function call SlavoGermaic was called repeatedly in the process function,
 * it is now only called once.
 *
 */
public class DoubleMeta implements Transformator {

	/**
	 * The replace list is used in the getSuggestions method. All of the letters in
	 * the misspelled word are replaced with the characters from this list to try
	 * and generate more suggestions, which implies l*n tries, if l is the size of
	 * the string, and n is the size of this list.
	 *
	 * In addition to that, each of these letters is added to the misspelled word.
	 */
	/*
	 * Metaphone 算法使用了 16 个辅音类，由下列字符代表：
	 * B X S K J T F H L M N P R 0 W Y 字符 0 是零，用来代表 th 的声音。
	 * 就像在 Soundex 算法里一样，第一个字母被保留，最后的代码被裁剪成四个字符，但是如果短于四个字符，也并不填充。
	 * 重复的字母和元音通常被删除，与元音的处理一样。Metaphone 算法整体上是一套规则集，可以把字母组合映射成辅音类。
	 * 
	 * DoubleMetaphone 算法对原来的辅音类做了一些修正,它把所有的开始元音都编码成 A，所以不再使用 Soundex 算法。
	 * 更加根本的变化是，DoubleMetaphone 被编写成可以为多音词返回不同的代码。
	 * 例如， hegemony中的 g 可以发轻声，也可以发重音，所以算法既返回 HJMN ，也可以返回 HKMN 。
	 * 除了这些例子之外，Metaphone 算法中的多数单词还是返回单一键。
	 * list26\list49\list96弃用
	 */
	private static char[] replaceList = { 'A', 'B', 'X', 'S', 'K', 'J', 'T', 'F', 'H', 'L', 'M', 'N', 'P', 'R', '0' };

	private static final String[] myList = { "GN", "KN", "PN", "WR", "PS", "" };
	private static final String[] list1 = { "ACH", "" };
	private static final String[] list2 = { "BACHER", "MACHER", "" };
	private static final String[] list3 = { "CAESAR", "" };
	private static final String[] list4 = { "CHIA", "" };
	private static final String[] list5 = { "CH", "" };
	private static final String[] list6 = { "CHAE", "" };
	private static final String[] list7 = { "HARAC", "HARIS", "" };
	private static final String[] list8 = { "HOR", "HYM", "HIA", "HEM", "" };
	private static final String[] list9 = { "CHORE", "" };
	private static final String[] list10 = { "VAN ", "VON ", "" };
	private static final String[] list11 = { "SCH", "" };
	private static final String[] list12 = { "ORCHES", "ARCHIT", "ORCHID", "" };
	private static final String[] list13 = { "T", "S", "" };
	private static final String[] list14 = { "A", "O", "U", "E", "" };
	private static final String[] list15 = { "L", "R", "N", "M", "B", "H", "F", "V", "W", " ", "" };
	private static final String[] list16 = { "MC", "" };
	private static final String[] list17 = { "CZ", "" };
	private static final String[] list18 = { "WICZ", "" };
	private static final String[] list19 = { "CIA", "" };
	private static final String[] list20 = { "CC", "" };
	private static final String[] list21 = { "I", "E", "H", "" };
	private static final String[] list22 = { "HU", "" };
	private static final String[] list23 = { "UCCEE", "UCCES", "" };
	private static final String[] list24 = { "CK", "CG", "CQ", "" };
	private static final String[] list25 = { "CI", "CE", "CY", "" };
	// DMV: used by the original code which returned two phonetic code, but not the
	// current code
	// private static final String[] list26 = {
	// "CIO", "CIE", "CIA", ""
	// };
	private static final String[] list27 = { " C", " Q", " G", "" };
	private static final String[] list28 = { "C", "K", "Q", "" };
	private static final String[] list29 = { "CE", "CI", "" };
	private static final String[] list30 = { "DG", "" };
	private static final String[] list31 = { "I", "E", "Y", "" };
	private static final String[] list32 = { "DT", "DD", "" };
	private static final String[] list33 = { "B", "H", "D", "" };
	private static final String[] list34 = { "B", "H", "D", "" };
	private static final String[] list35 = { "B", "H", "" };
	private static final String[] list36 = { "C", "G", "L", "R", "T", "" };
	private static final String[] list37 = { "EY", "" };
	private static final String[] list38 = { "LI", "" };
	private static final String[] list39 = { "ES", "EP", "EB", "EL", "EY", "IB", "IL", "IN", "IE", "EI", "ER", "" };
	private static final String[] list40 = { "ER", "" };
	private static final String[] list41 = { "DANGER", "RANGER", "MANGER", "" };
	private static final String[] list42 = { "E", "I", "" };
	private static final String[] list43 = { "RGY", "OGY", "" };
	private static final String[] list44 = { "E", "I", "Y", "" };
	private static final String[] list45 = { "AGGI", "OGGI", "" };
	private static final String[] list46 = { "VAN ", "VON ", "" };
	private static final String[] list47 = { "SCH", "" };
	private static final String[] list48 = { "ET", "" };

	// DMV: used by the orininal code which returned two phonetic code, but not the
	// current code
	// private static final String[] list49 = {
	// "IER ", ""
	// };
	private static final String[] list50 = { "JOSE", "" };
	private static final String[] list51 = { "SAN ", "" };
	private static final String[] list52 = { "SAN ", "" };
	private static final String[] list53 = { "JOSE", "" };
	private static final String[] list54 = { "L", "T", "K", "S", "N", "M", "B", "Z", "" };
	private static final String[] list55 = { "S", "K", "L", "" };
	private static final String[] list56 = { "ILLO", "ILLA", "ALLE", "" };
	private static final String[] list57 = { "AS", "OS", "" };
	private static final String[] list58 = { "A", "O", "" };
	private static final String[] list59 = { "ALLE", "" };
	private static final String[] list60 = { "UMB", "" };
	private static final String[] list61 = { "ER", "" };
	private static final String[] list62 = { "P", "B", "" };
	private static final String[] list63 = { "IE", "" };
	private static final String[] list64 = { "ME", "MA", "" };
	private static final String[] list65 = { "ISL", "YSL", "" };
	private static final String[] list66 = { "SUGAR", "" };
	private static final String[] list67 = { "SH", "" };
	private static final String[] list68 = { "HEIM", "HOEK", "HOLM", "HOLZ", "" };
	private static final String[] list69 = { "SIO", "SIA", "" };
	private static final String[] list70 = { "SIAN", "" };
	private static final String[] list71 = { "M", "N", "L", "W", "" };
	private static final String[] list72 = { "Z", "" };
	private static final String[] list73 = { "Z", "" };
	private static final String[] list74 = { "SC", "" };
	private static final String[] list75 = { "OO", "ER", "EN", "UY", "ED", "EM", "" };
	private static final String[] list76 = { "ER", "EN", "" };
	private static final String[] list77 = { "I", "E", "Y", "" };
	private static final String[] list78 = { "AI", "OI", "" };
	private static final String[] list79 = { "S", "Z", "" };
	private static final String[] list80 = { "TION", "" };
	private static final String[] list81 = { "TIA", "TCH", "" };
	private static final String[] list82 = { "TH", "" };
	private static final String[] list83 = { "TTH", "" };
	private static final String[] list84 = { "OM", "AM", "" };
	private static final String[] list85 = { "VAN ", "VON ", "" };
	private static final String[] list86 = { "SCH", "" };
	private static final String[] list87 = { "T", "D", "" };
	private static final String[] list88 = { "WR", "" };
	private static final String[] list89 = { "WH", "" };
	private static final String[] list90 = { "EWSKI", "EWSKY", "OWSKI", "OWSKY", "" };
	private static final String[] list91 = { "SCH", "" };
	private static final String[] list92 = { "WICZ", "WITZ", "" };
	private static final String[] list93 = { "IAU", "EAU", "" };
	private static final String[] list94 = { "AU", "OU", "" };
	private static final String[] list95 = { "C", "X", "" };

	// DMV: used by the orininal code which returned two phonetic code, but not the
	// current code
	// private static final String[] list96 = {
	// "ZO", "ZI", "ZA", ""
	// };

	/**
	 * 斯拉夫日耳曼语判别
	 * 
	 * @param in 单词
	 * 
	 * @return 是否出现斯拉夫日耳曼语字符
	 */
	private final static boolean SlavoGermanic(String in) {
		// 如果出现以下任一字符则返回true
		if ((in.indexOf("W") > -1) || (in.indexOf("K") > -1) || (in.indexOf("CZ") > -1) || (in.indexOf("WITZ") > -1))
			return true;
		return false;
	}

	/**
	 * 添加辅音字母到语音代码
	 * 
	 * @param primary 语音代码
	 * @param main 要添加的辅音字母
	 */
	private final static void MetaphAdd(StringBuffer primary, String main) {
		if (main != null) {
			primary.append(main);
		}
	}

	/**
	 * 添加辅音字母到语音代码
	 *  
	 * @param primary 语音代码
	 * @param main 要添加的辅音字母
	 */
	private final static void MetaphAdd(StringBuffer primary, char main) {
		primary.append(main);
	}

	/**
	 * 判断是否为元音字母
	 * 
	 * @param in 字符串（单词）
	 * @param at 当前字符在字符串中的位置
	 * @param length 字符串长度
	 * 
	 * @return 是否为元音字母
	 */
	private final static boolean isVowel(String in, int at, int length) {
		if ((at < 0) || (at >= length))
			return false;
		char it = in.charAt(at);
		if ((it == 'A') || (it == 'E') || (it == 'I') || (it == 'O') || (it == 'U') || (it == 'Y'))
			return true;
		return false;
	}

	/**
	 * 用于匹配单词中的字符串
	 * 
	 * @param string 单词字符串
	 * @param start 开始位置
	 * @param length 要获取的字符串长度
	 * @param list 要匹配的字符串列表（用于匹配获取的字符串）
	 * 
	 * @return 返回是否匹配
	 */
	private final static boolean stringAt(String string, int start, int length, String[] list) {
		if ((start < 0) || (start >= string.length()) || list.length == 0)
			return false;
		//获取单词字符串 string 的字串
		String substr = string.substring(start, start + length);
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals(substr))
				return true;
		}
		return false;
	}

	/**
	 * Take the given word, and return the best phonetic hash for it. Vowels are
	 * minimized as much as possible, and consonants that have similiar sounds are
	 * converted to the same consonant for example, 'v' and 'f' are both converted
	 * to 'f'
	 * 
	 * @param word the text to transform
	 * @return the result of the phonetic transformation
	 */
	// 输入单词，返回最合适的语音散列
	/*
	 * 1. 输入英语单词。
	 * 2. 删除或忽略被编码单词中的非英语字母字符并把字母全部转换为大写形式。
	 * 3. 对词首的字母或字母组合进行编码前的预处理。
	 * 字母组合 AE-、GN-、KN-、PN- 或 WR- 位于词首时，删除其中的首字母；
	 * 删除位于词首的字母组合 WH- 中的 H；将位于词首的字母 X 替换成 S 。
	 * 4. 对相邻的重复字母进行消重处理。
	 * 5. 按照编码规则进行编码（生成语音代码）：
	 * (1) 元音字母 A、E、I、O、U 位于词首时予以保留，位于其它位置时则删除。
	 * (2) 对被编码单词的除首字母之外的所有辅音字母根据 Metaphone 的语音代码转换规则中规定的对应关系进行转换或删除处理。
	 * 6. 输出 Metaphone 语音代码。
	 */
	public final String transform(String word) {
		// 初始化语音代码长度为单词长度+5
		StringBuffer primary = new StringBuffer(word.length() + 5);
		// 把字母全部转换为大写形式并附加5个空格？
		String in = word.toUpperCase() + "     ";
		// 设定在字符串中的位置（0为词首）
		int current = 0;
		// 设定字符串的长度
		int length = in.length();
		// 如果字符串长度小于1则返回空
		if (length < 1)
			return "";
		// 设定结束位置
		int last = length - 1;
		// 判断是否有斯拉夫日耳曼语字符
		boolean isSlavoGermaic = SlavoGermanic(in);
		// 字母组合AE-、GN-、KN-、PN-或WR-（以myList为准）位于词首时，
		// 删除其中的首字母（位置前进一位，不存入语音代码中）
		// 如果前两位匹配myList
		if (stringAt(in, 0, 2, myList))
			current += 1;
		// 将位于词首的字母X替换成S并存到语音代码中
		if (in.charAt(0) == 'X') {
			MetaphAdd(primary, 'S');
			current += 1;
		}
		// 元音字母 A、E、I、O、U 位于词首时予以保留，位于其它位置时则删除
		while (current < length) {
			// 获取当前位置的字符（当前位置为3或2或1，词首为A E I O U时这里的current = 0）
			switch (in.charAt(current)) {
			case 'A':
			case 'E':
			case 'I':
			case 'O':
			case 'U':
			case 'Y':
				// 把词首的Y记为A加到语音代码中
				if (current == 0)
					MetaphAdd(primary, 'A');
				current += 1;
				break;
			case 'B':
				MetaphAdd(primary, 'P');
				// 如果后一位也是B则只添加一个P到语音代码中
				if (in.charAt(current + 1) == 'B')
					current += 2;
				else
					current += 1;
				break;
			// 带有变音符号的拉丁字母大写C（C/C++/Java source code	"\u00C7"）
			case '\u00C7':
				// 记为S加到语音代码中
				MetaphAdd(primary, 'S');
				current += 1;
				break;
			case 'C':
				// 如果不在单词前两位且往前两位不是元音且从前一位开始3个字母匹配list1且往后两位不是I、E
				// 或者往前第2位开始6个字母匹配list2
				if ((current > 1) && !isVowel(in, current - 2, length) && stringAt(in, (current - 1), 3, list1)
						&& (in.charAt(current + 2) != 'I') && (in.charAt(current + 2) != 'E')
						|| stringAt(in, (current - 2), 6, list2)) {
					// 记为K加入到语音代码中
					MetaphAdd(primary, 'K');
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果位于词首且从当前字母开始6个字母匹配list3
				if ((current == 0) && stringAt(in, current, 6, list3)) {
					// 记为S加入到语音代码中
					MetaphAdd(primary, 'S');
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果从当前字母开始4个字母匹配list4
				if (stringAt(in, current, 4, list4)) {
					// 则记为K加入到语音代码中
					MetaphAdd(primary, 'K');
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果从当前字母开始2个字母匹配list5
				if (stringAt(in, current, 2, list5)) {
					// 如果不是词首且从当前字母开始4个字母匹配list6
					if ((current > 0) && stringAt(in, current, 4, list6)) {
						// 记为K加入到语音代码中
						MetaphAdd(primary, 'K');
						// 忽略后一位
						current += 2;
						break;
					}
					// 如果位于词首且从后一个字母开始5个字母匹配list7
					// 或从后一个字母开始3个字母匹配list8且单词的前5个字母不匹配list9
					if ((current == 0) && stringAt(in, (current + 1), 5, list7)
							|| stringAt(in, current + 1, 3, list8) && !stringAt(in, 0, 5, list9)) {
						// 记为K加入到语音代码中
						MetaphAdd(primary, 'K');
						// 忽略后一位
						current += 2;
						break;
					}
					// 如果单词的前4个字母匹配list10或单词的前三个字母匹配list11或往前第2个字母开始6个字母匹配list12
					// 或者往后第2个字母匹配list13或者(前面的字母匹配list14或当前位于词首)并且往后第2个字母匹配list15
					if (stringAt(in, 0, 4, list10) || stringAt(in, 0, 3, list11) || stringAt(in, current - 2, 6, list12)
							|| stringAt(in, current + 2, 1, list13)
							|| (stringAt(in, current - 1, 1, list14) || (current == 0))
									&& stringAt(in, current + 2, 1, list15)) {
						// 记为K加入到语音代码中
						MetaphAdd(primary, 'K');
					} else {
						// 如果不在词首
						if (current > 0) {
							// 如果单词前两个字母匹配list16
							if (stringAt(in, 0, 2, list16))
								// 记为K加入到语音代码中
								MetaphAdd(primary, 'K');
							else
								// 记为X加入到语音代码中
								MetaphAdd(primary, 'X');
						} else {
							// 记为X加入到语音代码中
							MetaphAdd(primary, 'X');
						}
					}
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果从当前开始2个字母匹配list17且从当前开始4个字母不匹配list18
				if (stringAt(in, current, 2, list17) && !stringAt(in, current, 4, list18)) {
					// 记为S加入到语音代码中
					MetaphAdd(primary, 'S');
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果从当前开始2个字母匹配list19
				if (stringAt(in, current, 2, list19)) {
					// 记为X加入到语音代码中
					MetaphAdd(primary, 'X');
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果从当前开始2个字母匹配list20且不是（在单词第2个字母的位置且单词首字母为M：MC）
				if (stringAt(in, current, 2, list20) && !((current == 1) && in.charAt(0) == 'M')) {
					// 如果往后第2个字母匹配list21且往后第2个字母开始2个字母不匹配list22
					if (stringAt(in, current + 2, 1, list21) && !stringAt(in, current + 2, 2, list22)) {
						// 如果（当前位置为2且上一个位置为A：AC） 或 从前一个字母开始5个字母匹配list23
						if (((current == 1) && (in.charAt(current - 1) == 'A'))
								|| stringAt(in, (current - 1), 5, list23))
							// 记为KS加入到语音代码中
							MetaphAdd(primary, "KS");
						else
							// 记为X加入到语音代码中
							MetaphAdd(primary, 'X');
						// 忽略后两位
						current += 3;
						break;
					} else {
						// 记为K加入到语音代码中
						MetaphAdd(primary, 'K');
						// 忽略后一位
						current += 2;
						break;
					}
				}
				// 如果从当前字母开始2个字母匹配list24
				if (stringAt(in, current, 2, list24)) {
					// 记为K加入到语音代码中
					MetaphAdd(primary, 'K');
					// 忽略后一位
					current += 2;
					break;
				// 如果从当前字母开始2个字母匹配list25
				} else if (stringAt(in, current, 2, list25)) {
					// 记为S加入到语音代码中
					MetaphAdd(primary, 'S');
					// 忽略后一位
					current += 2;
					break;
				}
				// 记为K加入到语音代码中
				MetaphAdd(primary, 'K');
				// 如果从后一个字母开始2个字母匹配list27
				if (stringAt(in, current + 1, 2, list27))
					// 忽略后两位
					current += 3;
				// 如果后一个字母匹配list28且后一个字母开始2个字母不匹配list29
				else if (stringAt(in, current + 1, 1, list28) && !stringAt(in, current + 1, 2, list29))
					// 忽略后一位
					current += 2;
				else
					current += 1;
				break;
			case 'D':
				// 如果从当前字母开始2个字母匹配list30
				if (stringAt(in, current, 2, list30)) {
					// 如果往后第2个字母匹配list31
					if (stringAt(in, current + 2, 1, list31)) {
						// 记为J加入到语音代码中
						MetaphAdd(primary, 'J');
						// 忽略后两位
						current += 3;
						break;
					} else {
						// 记为TK加入到语音代码中
						MetaphAdd(primary, "TK");
						// 忽略后一位
						current += 2;
						break;
					}
				}
				// 记为T加入到语音代码中
				MetaphAdd(primary, 'T');
				// 如果从当前字母开始2个字母匹配list32
				if (stringAt(in, current, 2, list32)) {
					// 忽略后一位
					current += 2;
				} else {
					current += 1;
				}
				break;
			case 'F':
				// 如果后一位也是F
				if (in.charAt(current + 1) == 'F')
					// 忽略后一位
					current += 2;
				else
					current += 1;
				// 记为F加入到语音代码中
				MetaphAdd(primary, 'F');
				break;
			case 'G':
				// 如果后一位是H
				if (in.charAt(current + 1) == 'H') {
					// 如果不是词首且前一个字母不是元音
					if ((current > 0) && !isVowel(in, current - 1, length)) {
						// 记为K加入到语音代码中
						MetaphAdd(primary, 'K');
						// 忽略后一位
						current += 2;
						break;
					}
					// 如果当前位置在单词前三位
					if (current < 3) {
						// 如果位于词首
						if (current == 0) {
							// 如果往后第二位为I
							if (in.charAt(current + 2) == 'I')
								// 记为J加入到语音代码中
								MetaphAdd(primary, 'J');
							else
								// 记为K加入到语音代码中
								MetaphAdd(primary, 'K');
							// 忽略后一位
							current += 2;
							break;
						}
					}
					// 如果不在单词前2位且往前第2位匹配list33
					// 或不在单词前3位且往前第3位匹配list34
					// 或不在单词前4位且往前第4位匹配list35
					if ((current > 1) && stringAt(in, current - 2, 1, list33)
							|| ((current > 2) && stringAt(in, current - 3, 1, list34))
							|| ((current > 3) && stringAt(in, current - 4, 1, list35))) {
						// 忽略后一位
						current += 2;
						break;
					} else {
						// 如果不在单词前3位且前1位不是U且往前第3位匹配list36
						if ((current > 2) && (in.charAt(current - 1) == 'U') && stringAt(in, current - 3, 1, list36)) {
							// 记为F加入到语音代码中
							MetaphAdd(primary, 'F');
						} else {
							// 如果不在词首且前1位不是I
							if ((current > 0) && (in.charAt(current - 1) != 'I'))
								// 记为K加入到语音代码中
								MetaphAdd(primary, 'K');
						}
						// 忽略后一位
						current += 2;
						break;
					}
				}
				// 如果后一位是N
				if (in.charAt(current + 1) == 'N') {
					// 如果当前是单词的第二个字母且首字母是元音且不是斯拉夫日耳曼语
					if ((current == 1) && isVowel(in, 0, length) && !isSlavoGermaic) {
						// 记为KN加入到语音代码中
						MetaphAdd(primary, "KN");
					} else {
						// 如果往后第二位开始2个字母匹配list37且后一个字母不是Y且不是斯拉夫日耳曼语
						if (!stringAt(in, current + 2, 2, list37) && (in.charAt(current + 1) != 'Y')
								&& !isSlavoGermaic) {
							// 记为N加入到语音代码中
							MetaphAdd(primary, "N");
						} else {
							// 记为KN加入到语音代码中
							MetaphAdd(primary, "KN");
						}
					}
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果从后一位开始2个字母匹配list38且不是斯拉夫日耳曼语
				if (stringAt(in, current + 1, 2, list38) && !isSlavoGermaic) {
					// 记为KL加入到语音代码中
					MetaphAdd(primary, "KL");
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果不在词首且后一位不是Y或从后一位开始2个字母匹配list39
				if ((current == 0) && ((in.charAt(current + 1) == 'Y') || stringAt(in, current + 1, 2, list39))) {
					// 记为K加入到语音代码中
					MetaphAdd(primary, 'K');
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果从后一位开始2个字母匹配list40或后一位是Y
				// 且单词前6个字母不匹配list41且前一个字母不匹配list42且从前一个字母开始3个字母不匹配list43
				if ((stringAt(in, current + 1, 2, list40) || (in.charAt(current + 1) == 'Y'))
						&& !stringAt(in, 0, 6, list41) && !stringAt(in, current - 1, 1, list42)
						&& !stringAt(in, current - 1, 3, list43)) {
					// 记为K加入到语音代码中
					MetaphAdd(primary, 'K');
					// 忽略后一个字母
					current += 2;
					break;
				}
				// 如果后一个字母匹配list44或从前一个字母开始4个字母匹配list45
				if (stringAt(in, current + 1, 1, list44) || stringAt(in, current - 1, 4, list45)) {
					// 如果单词前四个字母匹配list46或单词前三个字母匹配list47或从后一个字母开始2个字母匹配list48
					if (stringAt(in, 0, 4, list46) || stringAt(in, 0, 3, list47)
							|| stringAt(in, current + 1, 2, list48)) {
						// 记为K加入到语音代码中
						MetaphAdd(primary, 'K');
					} else {
						// 记为J加入到语音代码中
						MetaphAdd(primary, 'J');
					}
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果后一个字母是G
				if (in.charAt(current + 1) == 'G')
					// 忽略后一位
					current += 2;
				else
					current += 1;
				// 记为K加入到语音代码中
				MetaphAdd(primary, 'K');
				break;
			case 'H':
				// 如果（在词首或前一个字母是元音）且后一个字母是元音
				if (((current == 0) || isVowel(in, current - 1, length)) && isVowel(in, current + 1, length)) {
					// 记为H加入到语音代码中
					MetaphAdd(primary, 'H');
					// 忽略后一位
					current += 2;
				} else {
					current += 1;
				}
				break;
			case 'J':
				// 如果从当前字母开始4个字母匹配list50或单词前四个字母匹配list51
				if (stringAt(in, current, 4, list50) || stringAt(in, 0, 4, list51)) {
					// 如果在词首且往后第四个字母为空或单词前四个字母匹配list52
					if ((current == 0) && (in.charAt(current + 4) == ' ') || stringAt(in, 0, 4, list52)) {
						// 记为H加入到语音代码中
						MetaphAdd(primary, 'H');
					} else {
						// // 记为J加入到语音代码中
						MetaphAdd(primary, 'J');
					}
					current += 1;
					break;
				}
				// 如果在词首且从当前字母开始4个字母匹配list53
				if ((current == 0) && !stringAt(in, current, 4, list53)) {
					// 记为J加入到语音代码中
					MetaphAdd(primary, 'J');
				} else {
					// 如果前一个字母是元音且不是斯拉夫日耳曼语且（后一个字母是A或后一个字母是O）
					if (isVowel(in, current - 1, length) && !isSlavoGermaic
							&& ((in.charAt(current + 1) == 'A') || in.charAt(current + 1) == 'O')) {
						// 记为J加入到语音代码中
						MetaphAdd(primary, 'J');
					} else {
						// 如果是最后一个字母
						if (current == last) {
							// 记为J加入到语音代码中
							MetaphAdd(primary, 'J');
						} else {
							// 如果后一位匹配list54且前一位匹配list55
							if (!stringAt(in, current + 1, 1, list54) && !stringAt(in, current - 1, 1, list55)) {
								// 记为J加入到语音代码中
								MetaphAdd(primary, 'J');
							}
						}
					}
				}
				// 如果后一位是J
				if (in.charAt(current + 1) == 'J')
					// 忽略后一位
					current += 2;
				else
					current += 1;
				break;
			case 'K':
				// 如果后一位是K
				if (in.charAt(current + 1) == 'K')
					// 忽略后一位
					current += 2;
				else
					current += 1;
				// 记为K加入到语音代码中
				MetaphAdd(primary, 'K');
				break;
			case 'L':
				// 如果后一位是L
				if (in.charAt(current + 1) == 'L') {
					// 如果当前位置在单词倒数第四位且从前一位开始4个字母匹配list56
					// 或（单词最后两个字母匹配list57或单词最后一位匹配list58）且从当前位置的前一个字母开始4个字母匹配list59
					if (((current == (length - 3)) && stringAt(in, current - 1, 4, list56))
							|| ((stringAt(in, last - 1, 2, list57) || stringAt(in, last, 1, list58))
									&& stringAt(in, current - 1, 4, list59))) {
						// 记为L加入到语音代码中
						MetaphAdd(primary, 'L');
						// 忽略后一位
						current += 2;
						break;
					}
					// 忽略后一位
					current += 2;
				} else
					current += 1;
				// 记为L加入到语音代码中
				MetaphAdd(primary, 'L');
				break;
			case 'M':
				// 如果从前一个字母开始3个字母匹配list60且后一个字母是单词的结束或从往后第二个字母开始两个字母匹配list60
				// 或后一个字母是M
				if ((stringAt(in, current - 1, 3, list60)
						&& (((current + 1) == last) || stringAt(in, current + 2, 2, list61)))
						|| (in.charAt(current + 1) == 'M'))
					// 忽略后一位
					current += 2;
				else
					current += 1;
				// 记为M加入到语音代码中
				MetaphAdd(primary, 'M');
				break;
			case 'N':
				// 如果后一个字母是N
				if (in.charAt(current + 1) == 'N')
					// 忽略后一位
					current += 2;
				else
					current += 1;
				// 记为N加入到语音代码中
				MetaphAdd(primary, 'N');
				break;
			// 带波浪号的大写拉丁字母N（C/C++/Java source code	"\u00D1"）
			case '\u00D1':
				current += 1;
				// 记为N加入到语音代码中
				MetaphAdd(primary, 'N');
				break;
			case 'P':
				// 如果后一位是N
				if (in.charAt(current + 1) == 'N') {
					// 记为F加入到语音代码中
					MetaphAdd(primary, 'F');
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果后一位匹配list62
				if (stringAt(in, current + 1, 1, list62))
					// 忽略后一位
					current += 2;
				else
					current += 1;
				// 记为P加入到语音代码中
				MetaphAdd(primary, 'P');
				break;
			case 'Q':
				// 如果后一位是Q
				if (in.charAt(current + 1) == 'Q')
					// 忽略后一位
					current += 2;
				else
					current += 1;
				// 记为K加入到语音代码中
				MetaphAdd(primary, 'K');
				break;
			case 'R':
				// 如果当前是最后一位且不是斯拉夫日耳曼语且前面两个字母匹配list63且从往前第四个字母开始两个字母匹配list64
				if ((current == last) && !isSlavoGermaic && stringAt(in, current - 2, 2, list63)
						&& !stringAt(in, current - 4, 2, list64)) {
					// MetaphAdd(primary, "");
				} else
					// 记为R加入到语音代码中
					MetaphAdd(primary, 'R');
				// 如果后一位是R
				if (in.charAt(current + 1) == 'R')
					// 忽略后一位
					current += 2;
				else
					current += 1;
				break;
			case 'S':
				// 如果从前一个字母开始3个字母匹配list65
				if (stringAt(in, current - 1, 3, list65)) {
					current += 1;
					break;
				}
				// 如果位于词首且从当前字母开始5个字母匹配list66
				if ((current == 0) && stringAt(in, current, 5, list66)) {
					// 记为X加入到语音代码中
					MetaphAdd(primary, 'X');
					current += 1;
					break;
				}
				// 如果从当前字母开始2个字母匹配list67
				if (stringAt(in, current, 2, list67)) {
					// 如果从后一个字母开始4个字母匹配list68
					if (stringAt(in, current + 1, 4, list68))
						// 记为S加入到语音代码中
						MetaphAdd(primary, 'S');
					else
						// 记为X加入到语音代码中
						MetaphAdd(primary, 'X');
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果从当前字母开始3个字母匹配list69或从当前字母开始4个字母匹配list70
				if (stringAt(in, current, 3, list69) || stringAt(in, current, 4, list70)) {
					// 记为S加入到语音代码中
					MetaphAdd(primary, 'S');
					// 忽略后两位
					current += 3;
					break;
				}
				// 如果(位于词首且后一个字母匹配list71)或后一个字母匹配list72
				if (((current == 0) && stringAt(in, current + 1, 1, list71)) || stringAt(in, current + 1, 1, list72)) {
					// 记为S加入到语音代码中
					MetaphAdd(primary, 'S');
					// 后一个字母匹配list73
					if (stringAt(in, current + 1, 1, list73))
						// 忽略后一位
						current += 2;
					else
						current += 1;
					break;
				}
				// 如果从当前字母开始2个字母匹配list74
				if (stringAt(in, current, 2, list74)) {
					// 如果往后第2个字母匹配H
					if (in.charAt(current + 2) == 'H')
						// 如果从往后第3个字母开始2个字母匹配list75
						if (stringAt(in, current + 3, 2, list75)) {
							// 如果从往后第3个字母开始2个字母匹配list76
							if (stringAt(in, current + 3, 2, list76)) {
								// 记为X加入到语音代码中
								MetaphAdd(primary, "X");
							} else {
								// 记为SK加入到语音代码中
								MetaphAdd(primary, "SK");
							}
							// 忽略后两位
							current += 3;
							break;
						} else {
							// 记为X加入到语音代码中
							MetaphAdd(primary, 'X');
							// 忽略后两位
							current += 3;
							break;
						}
					// 如果往后第2个字母匹配list77
					if (stringAt(in, current + 2, 1, list77)) {
						// 记为S加入到语音代码中
						MetaphAdd(primary, 'S');
						// 忽略后两位
						current += 3;
						break;
					}
					// 记为SK加入到语音代码中
					MetaphAdd(primary, "SK");
					// 忽略后两位
					current += 3;
					break;
				}
				// 如果当前是最后一位且前两个字母匹配list78
				if ((current == last) && stringAt(in, current - 2, 2, list78)) {
					// MetaphAdd(primary, "");
				} else
					// 记为S加入到语音代码中
					MetaphAdd(primary, 'S');
				// 如果后一位匹配list79
				if (stringAt(in, current + 1, 1, list79))
					// 忽略后一位
					current += 2;
				else
					current += 1;
				break;
			case 'T':
				// 如果从当前字母开始4个字母匹配list80
				if (stringAt(in, current, 4, list80)) {
					// 记为X加入到语音代码中
					MetaphAdd(primary, 'X');
					// 忽略后两位
					current += 3;
					break;
				}
				// 如果从当前字母开始3个字母匹配list81
				if (stringAt(in, current, 3, list81)) {
					// 记为X加入到语音代码中
					MetaphAdd(primary, 'X');
					// 忽略后两位
					current += 3;
					break;
				}
				// 如果从当前字母开始2个字母匹配list82或从当前字母开始3个字母匹配list83
				if (stringAt(in, current, 2, list82) || stringAt(in, current, 3, list83)) {
					// 如果从往后第2个字母开始两个字母匹配list84或单词的前4个字母匹配list85或单词的前3个字母匹配list86
					if (stringAt(in, (current + 2), 2, list84) || stringAt(in, 0, 4, list85)
							|| stringAt(in, 0, 3, list86)) {
						// 记为T加入到语音代码中
						MetaphAdd(primary, 'T');
					} else {
						// 记为0(th)加入到语音代码中
						MetaphAdd(primary, '0');
					}
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果后一个字母匹配list87
				if (stringAt(in, current + 1, 1, list87)) {
					// 忽略后一位
					current += 2;
				} else
					current += 1;
				// 记为T加入到语音代码中
				MetaphAdd(primary, 'T');
				break;
			case 'V':
				// 如果后一个字母是V
				if (in.charAt(current + 1) == 'V')
					// 忽略后一位
					current += 2;
				else
					current += 1;
				// 记为F加入到语音代码中
				MetaphAdd(primary, 'F');
				break;
			case 'W':
				// 如果从当前字母开始2个字母匹配list88
				if (stringAt(in, current, 2, list88)) {
					// 记为R加入到语音代码中
					MetaphAdd(primary, 'R');
					// 忽略后一位
					current += 2;
					break;
				}
				// 如果位于词首且（后一个字母是元音或从当前字母开始2个字母匹配list89）
				if ((current == 0) && (isVowel(in, current + 1, length) || stringAt(in, current, 2, list89))) {
					// 记为A加入到语音代码中
					MetaphAdd(primary, 'A');
				}
				// 如果（当前是最后一位且前一位是元音）或从前一个字母开始5个字母匹配list90或单词前三位匹配list91
				if (((current == last) && isVowel(in, current - 1, length)) || stringAt(in, current - 1, 5, list90)
						|| stringAt(in, 0, 3, list91)) {
					// 记为F加入到语音代码中
					MetaphAdd(primary, 'F');
					current += 1;
					break;
				}
				// 如果从当前字母开始4个字母匹配list92
				if (stringAt(in, current, 4, list92)) {
					// 记为TS加入到语音代码中
					MetaphAdd(primary, "TS");
					// 忽略后三位
					current += 4;
					break;
				}
				current += 1;
				break;
			case 'X':
				// 如果当前不是（词尾且（前三个字母匹配list93或前两个字母匹配list94））
				if (!((current == last)
						&& (stringAt(in, current - 3, 3, list93) || stringAt(in, current - 2, 2, list94))))
					// 记为KS加入到语音代码中
					MetaphAdd(primary, "KS");
				// 如果后一个字母匹配list95
				if (stringAt(in, current + 1, 1, list95))
					// 忽略后一位
					current += 2;
				else
					current += 1;
				break;
			case 'Z':
				// 如果后一个字母是H
				if (in.charAt(current + 1) == 'H') {
					// 记为J加入到语音代码中
					MetaphAdd(primary, 'J');
					// 忽略后一位
					current += 2;
					break;
				} else {
					// 记为S加入到语音代码中
					MetaphAdd(primary, 'S');
				}
				// 如果后一位是Z
				if (in.charAt(current + 1) == 'Z')
					// 忽略后一位
					current += 2;
				else
					current += 1;
				break;
			default:
				current += 1;
			}
		}
		// 返回语音代码
		return primary.toString();
	}

	/**
	 * @see com.swabunga.spell.engine.Transformator#getReplaceList()
	 */
	public char[] getReplaceList() {
		return replaceList;
	}
}
