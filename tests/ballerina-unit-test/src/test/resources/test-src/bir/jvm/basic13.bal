function foo(int a, string b) returns string {
   int a1 = 34;
   string s1 = "R";
   string s2 = "W";
   string s3 = "Q";
   if (a1 == 23) {
      s1 = s2;
   } else if (a1 == a) {
      s1 = b;
   }
   return s1;
}
