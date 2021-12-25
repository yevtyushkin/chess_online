import 'package:desktop/desktop.dart';

class RoomCard extends StatelessWidget {
  const RoomCard({
    Key? key,
    required this.onPressed,
    required this.title,
    this.backgroundColor,
  }) : super(key: key);

  final VoidCallback onPressed;
  final Widget title;
  final Color? backgroundColor;

  @override
  Widget build(BuildContext context) {
    return Button(
      onPressed: onPressed,
      body: SizedBox(
        height: 300,
        width: 250,
        child: DecoratedBox(
          decoration: BoxDecoration(
            color: backgroundColor ?? const Color(0xFF1F1F1F),
            borderRadius: const BorderRadius.all(Radius.circular(5)),
          ),
          child: Padding(
            padding: const EdgeInsets.all(8.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                title,
                DecoratedBox(
                  position: DecorationPosition.foreground,
                  decoration: BoxDecoration(
                    border: Border.all(color: const Color(0xFF292929)),
                  ),
                  child: Image.asset('assets/images/chessboard.png'),
                )
              ],
            ),
          ),
        ),
      ),
    );
  }
}
