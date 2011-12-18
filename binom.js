var Binom = {
    choose: function(n, k) {
        if (n < 0 || k < 0)
            throw "Unable to choose less than zero.";
        if (n == 0) return 0;
        if (n == k || k == 0) return 1;
        return this.choose(n - 1, k - 1) + this.choose(n - 1, k);
   },
   sum: function(n, k) {
       if (k == 0) return 1;
       return this.sum(n, k - 1) + this.choose(n, k);
   }
};

Binom.version = "0.1";