if (typeof String.prototype.reverse == "undefined")
    String.prototype.reverse = function() {
        return this.split("").reverse().join("");
    };

var Bankers = {
    next: function(b) {
        var z = "", y = "", next = "";
        
        var comp = /(1*)(0*)(1[01]*)?/.exec(b.reverse());
        comp.reverse();
        
        if (comp.length > 1 && comp[1])
            z = comp[1];
        if (comp.length > 2 && comp[2])
            y = comp[2];
        if (comp[0])
            next = comp[0].reverse().substring(0, comp[0].length - 1) + "0";
            
        next += y + "1";
        if (z.length > 0)
            next += z.substring(1);
            
        return next;
    },
    to: function(a, n) {
        if (typeof a == "string") {
            n = n || a.length;
            a = parseInt(a, 2);
        }
        if (!n)
            throw "Unable to reach Banker's number without length.";
        var c = 0, b = 0, e = a;
        if (a == 0)
            return 0;
            
        do {
            e -= Binom.choose(n, c++);
        } while (Binom.choose(n, c) <= a);
        c--;
        n--;
        if (Binom.choose(n, c))
            b++;
        while (c >= 0) {
            if (b % 2 === 0)
                e -= Binom.choose(n, c);
            b = b << 1;
            if (Binom.choose(n-- + 1, c--)) 
                b++;
        }
        return b;
    },
    from: function(b) {
        var bits = b.split("");
        var n = b.length, a = 0, c = 0, cn = (b.match(/1/g) || []).length;
        for (var i = 0; i < bits.length && c < cn; i++) {
            if (bits[i] == "1")
                a += Binom.choose(n, c++);
            else
                a += Binom.choose(n - i - 1, cn - c - 1);
        }
        return a;
    }
};
Bankers.version = "0.1";