let
  nixpkgs = builtins.fetchTarball {
    #url = "channel:nixos-21.05";
    url = "https://github.com/NixOS/nixpkgs/archive/2d6ab6c6b92f7aaf8bc53baba9754b9bfdce56f2.tar.gz";
    #sha256 = "0l975q132x08qvw73qj391dl6qk9a661my8njcg5sl5rcmna3bmj";
  };  pkgs = import nixpkgs { };
  run-jekyll = pkgs.writeScript "run-jekyll" ''
    jekyll serve -s modules/microsite/target/site --baseurl /sharry
  '';
in
pkgs.mkShell {
  buildInputs = with pkgs; [
    jekyll nodejs
  ];
  shellHook = ''
    alias jekyll-sharry=${run-jekyll}
  '';
}
