function foo(int a) returns int {
   int b = 6;
   if (a < 4) {
      b = bar(5);
   } else {
      b = baz(8);
   }
   return b;
}

function bar(int a) returns int {
   return a + 7;
}

function baz(int a) returns int {
   return a + 3;
}
