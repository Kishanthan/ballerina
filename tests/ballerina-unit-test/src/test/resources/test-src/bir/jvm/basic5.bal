function foo(int a) returns int {
   int i = 0;
   int b;
   while (i < 1000) {
      b = fn1(a);
      i++;
   }
   return b;
}

function fn1(int a) returns int {
   int b = 6;
   if (a < 4) {
      b = fn2(5);
   } else {
      b = fn3(8);
   }
   return b;
}

function fn2(int a) returns int {
   return a + 7;
}

function fn3(int a) returns int {
   return a + 3;
}
