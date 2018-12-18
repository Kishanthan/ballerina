function foo(int a) returns int {
   int d;
   if (a == 1) {
       d = 1;
   } else {
       d = bar(a - 1);
   }
   return d;
}

function bar(int b) returns int {
   return 5 + b;
}
